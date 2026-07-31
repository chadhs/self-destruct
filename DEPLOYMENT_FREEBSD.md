# self-destruct FreeBSD Deployment

Deploy self-destruct on a FreeBSD home server using the same pattern as
DoThisWeek: dedicated app user, PostgreSQL, `.env`, Leiningen uberjar, `rc.d`
service, nginx reverse proxy, certbot, and daily DB backups.

## Assumptions

- PostgreSQL, nginx, certbot, firewall rules, and (optionally) Cloudflare DDNS
  are already available on the server.
- `sudo` is available.
- The server has outbound HTTPS for GitHub, Maven/Clojars, Cloudflare, and
  Let's Encrypt.
- App path is `/home/selfdestruct/self-destruct`.
- App user is `selfdestruct`.
- App port is `4003` (DoThisWeek uses `4002`).

## 1. Install Java Runtime and Build Tools

```sh
sudo pkg install openjdk21 git
```

Install Leiningen if it is not already on the system (e.g. `sudo pkg install
leiningen`, or place a `lein` script on `PATH`).

Verify:

```sh
/usr/local/openjdk21/bin/java -version
lein version
```

The deployment scripts force `JAVA_HOME=/usr/local/openjdk21` and put
`/usr/local/openjdk21/bin` first in `PATH` so FreeBSD's `javavm` wrapper does
not accidentally select an older Java runtime.

## 2. Create App User and Clone Repo

```sh
sudo pw user add selfdestruct -m -s /bin/sh -c "self-destruct Application"
```

If the repo is private, create a deploy key before cloning:

```sh
sudo -u selfdestruct -H sh
mkdir -p ~/.ssh
chmod 700 ~/.ssh
ssh-keygen -t ed25519 -C "selfdestruct-server@$(hostname)" -f ~/.ssh/id_ed25519 -N ""
cat ~/.ssh/id_ed25519.pub
exit
```

Add the printed public key as a read-only deploy key in GitHub
(Repository Settings → Deploy keys), then test and clone:

```sh
sudo -u selfdestruct ssh -T git@github.com
sudo -u selfdestruct git clone git@github.com:chadhs/self-destruct.git /home/selfdestruct/self-destruct
```

After the clone, you can regenerate or reprint the key with
`./scripts/setup-deploy-key.sh` as the `selfdestruct` user.

## 3. Create PostgreSQL Database

```sh
sudo -u postgres createuser selfdestruct
sudo -u postgres createdb -O selfdestruct selfdestruct_prod
sudo -u postgres psql -c "ALTER USER selfdestruct WITH PASSWORD 'YOUR_SECURE_PASSWORD';"
```

Verify the app user can connect:

```sh
psql -U selfdestruct -h localhost selfdestruct_prod
```

If needed, update `pg_hba.conf` to allow `md5` or `scram-sha-256`
authentication for localhost connections, matching the other deployed apps.

## 4. Create Production Environment

Run this as the app user from the repo root:

```sh
sudo -u selfdestruct sh -c 'cd /home/selfdestruct/self-destruct && ./scripts/create-env.sh'
```

The script prompts for:

- Database password for the `selfdestruct` PostgreSQL user
- Domain name
- App port (default `4003`)

It writes `/home/selfdestruct/self-destruct/.env` with mode `600`, including
generated `SESSION_COOKIE_KEY` (16 hex chars) and `DATABASE_ENCRYPTION_KEY`.

See [`.env.example`](.env.example) for the full variable list.

## 5. Build and Migrate

Run the production build as the app user:

```sh
sudo -u selfdestruct sh -c 'cd /home/selfdestruct/self-destruct && ./scripts/build-prod.sh'
```

This:

- Fetches dependencies with `lein deps`
- Builds `target/self-destruct.jar` with `lein uberjar`
- Runs migrations with `java -jar target/self-destruct.jar --migrate`

Optional local server test from the FreeBSD host:

```sh
sudo -u selfdestruct sh -c 'cd /home/selfdestruct/self-destruct && ./scripts/start-app.sh'
```

From another shell on the server:

```sh
curl -s http://127.0.0.1:4003/ | head -20
```

Do not test with `http://SERVER_IP:4003`. Production `.env` sets `HOST=127.0.0.1`,
so external browser traffic should go through nginx.

## 6. Install and Start rc.d Service

Run as root:

```sh
sudo /home/selfdestruct/self-destruct/scripts/create-service.sh
sudo sysrc selfdestruct_enable="YES"
sudo service selfdestruct start
```

Verify:

```sh
sudo service selfdestruct status
curl -s http://127.0.0.1:4003/ | head -20
sudo tail -f /var/log/selfdestruct.log
```

## 7. Configure Nginx

Run as root:

```sh
sudo /home/selfdestruct/self-destruct/scripts/setup-nginx.sh
```

Enter your domain when prompted. The script creates a site config that proxies
the apex and `www` hostnames to `127.0.0.1:4003`.

Test and reload nginx:

```sh
sudo service nginx configtest
sudo service nginx reload
```

If nginx is not already enabled:

```sh
sudo sysrc nginx_enable="YES"
sudo service nginx start
```

## 8. Configure DNS and Cloudflare DDNS

In Cloudflare (or your DNS provider):

- Create an A record for `@` pointing to the server public IP
- Prefer a CNAME for `www` pointing at the apex hostname
- Keep records DNS-only unless you intentionally want a CDN proxy

If you already run a shared Cloudflare DDNS script on this host, add another
zone entry for this domain (same pattern as DoThisWeek / other apps). Example:

```sh
sudo sysrc cloudflare_ddns_zoneN_id="YOUR_ZONE_ID"
sudo sysrc cloudflare_ddns_zoneN_record_id="YOUR_RECORD_ID"
sudo sysrc cloudflare_ddns_zoneN_name="your.domain.example"
```

Then update the DDNS script to call `process_zone` for the new zone and verify:

```sh
sudo /usr/local/bin/cloudflare-ddns.sh
tail /var/log/cloudflare-ddns.log
```

## 9. Configure HTTPS

Once DNS points at the server:

```sh
dig your.domain.example
dig www.your.domain.example
sudo certbot --nginx -d your.domain.example -d www.your.domain.example
```

Verify HTTPS:

```sh
curl -I https://your.domain.example
```

Certbot renewal should already be configured if other apps use it. Verify:

```sh
sudo certbot renew --dry-run
```

## 10. Configure Database Backups

Run as root:

```sh
sudo /home/selfdestruct/self-destruct/scripts/backup-setup.sh
```

This creates:

- Backup directory: `/home/selfdestruct/backups/`
- Backup script: `/home/selfdestruct/backup-db.sh`
- Cron job: daily at 3:45 AM
- Log file: `/var/log/selfdestruct-backup.log`
- 30-day backup retention

The generated backup script loads `.env` and uses `DB_PASSWORD` for `pg_dump`.

Test manually:

```sh
sudo -u selfdestruct /home/selfdestruct/backup-db.sh
ls -la /home/selfdestruct/backups/
tail /var/log/selfdestruct-backup.log
```

## Future Deployments

Run as root:

```sh
sudo /home/selfdestruct/self-destruct/scripts/update-restart.sh
```

This runs `git pull`, rebuilds the uberjar, runs migrations, and restarts the
service.

Schema changes should be backward-compatible with the currently running app,
because `update-restart.sh` runs migrations before restarting the service.

## Troubleshooting

If the build fails with an error like:

```text
has been compiled by a more recent version of the Java Runtime
this version of the Java Runtime only recognizes class file versions up to 52.0
```

the build is running under Java 8. Confirm OpenJDK 21 is installed:

```sh
/usr/local/openjdk21/bin/java -version
```

Then pull the latest scripts and rerun `./scripts/build-prod.sh`. The production
scripts explicitly use `/usr/local/openjdk21/bin/java`.

Health check (once HTTPS is up):

```sh
curl -s https://your.domain.example/health
```

## Quick Reference

| Item | Value |
|------|-------|
| App port | 4003 |
| DB name | selfdestruct_prod |
| DB user | selfdestruct |
| System user | selfdestruct |
| App directory | /home/selfdestruct/self-destruct |
| Uberjar | target/self-destruct.jar |
| Service name | selfdestruct |
| App log | /var/log/selfdestruct.log |
| Backup log | /var/log/selfdestruct-backup.log |
| Backup time | 3:45 AM daily |
| Backup retention | 30 days |
| Build | `lein uberjar` |
| Migrate | `java -jar target/self-destruct.jar --migrate` |
