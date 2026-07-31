#!/bin/sh

echo "Setting up database backups..."

# Check if running as root
if [ "$(id -u)" != "0" ]; then
    echo "Error: This script must be run as root (use sudo)"
    exit 1
fi

BACKUP_DIR="/home/selfdestruct/backups"
mkdir -p ${BACKUP_DIR}
chown selfdestruct:selfdestruct ${BACKUP_DIR}
chmod 750 ${BACKUP_DIR}

cat > /home/selfdestruct/backup-db.sh << 'EOF'
#!/bin/sh

# Configuration
DB_NAME="selfdestruct_prod"
DB_USER="selfdestruct"
ENV_FILE="/home/selfdestruct/self-destruct/.env"
BACKUP_DIR="/home/selfdestruct/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/selfdestruct_backup_${DATE}.sql"

if [ -f "${ENV_FILE}" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
        case "$line" in
            ''|'#'*) continue ;;
            *'='*)
                key="${line%%=*}"
                value="${line#*=}"
                if echo "$key" | grep -q '^[A-Za-z_][A-Za-z0-9_]*$'; then
                    export "$key=$value"
                fi
                ;;
        esac
    done < "${ENV_FILE}"
fi

if [ -n "${DB_PASSWORD}" ]; then
    export PGPASSWORD="${DB_PASSWORD}"
fi

echo "Creating backup: ${BACKUP_FILE}"
pg_dump -U ${DB_USER} -h localhost ${DB_NAME} > ${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "Backup created successfully: ${BACKUP_FILE}"

    gzip ${BACKUP_FILE}
    echo "Backup compressed: ${BACKUP_FILE}.gz"

    find ${BACKUP_DIR} -name "selfdestruct_backup_*.sql.gz" -mtime +30 -delete
    echo "Cleaned up backups older than 30 days"
else
    echo "Backup failed"
    exit 1
fi
EOF

chmod +x /home/selfdestruct/backup-db.sh
chown selfdestruct:selfdestruct /home/selfdestruct/backup-db.sh

echo "Setting up daily backup cron job..."
CRON_LINE="45 3 * * * /home/selfdestruct/backup-db.sh >> /var/log/selfdestruct-backup.log 2>&1"

if ! crontab -u selfdestruct -l 2>/dev/null | grep -q "backup-db.sh"; then
    (crontab -u selfdestruct -l 2>/dev/null; echo "${CRON_LINE}") | crontab -u selfdestruct -
    echo "Daily backup cron job added for user 'selfdestruct'"
else
    echo "Daily backup cron job already exists for user 'selfdestruct'"
fi

touch /var/log/selfdestruct-backup.log
chown selfdestruct:selfdestruct /var/log/selfdestruct-backup.log
chmod 644 /var/log/selfdestruct-backup.log

echo ""
echo "Database backup system configured:"
echo "  Backup directory: ${BACKUP_DIR}"
echo "  Backup script: /home/selfdestruct/backup-db.sh"
echo "  Schedule: Daily at 3:45 AM"
echo "  Log file: /var/log/selfdestruct-backup.log"
echo "  Retention: 30 days"
echo ""
echo "To run a manual backup:"
echo "  sudo -u selfdestruct /home/selfdestruct/backup-db.sh"
echo ""
echo "To view backup logs:"
echo "  tail -f /var/log/selfdestruct-backup.log"
