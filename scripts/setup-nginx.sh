#!/bin/sh

echo "Setting up Nginx configuration..."

# Check if running as root
if [ "$(id -u)" != "0" ]; then
    echo "Error: This script must be run as root (use sudo)"
    exit 1
fi

echo "Please provide the following information:"
printf "Domain name (e.g., selfdestruct.example.com): "
read DOMAIN

if [ -z "$DOMAIN" ]; then
    echo "Error: Domain name is required"
    exit 1
fi

WWW_DOMAIN="www.${DOMAIN}"

echo "Creating Nginx site configuration..."
mkdir -p /usr/local/etc/nginx/sites-available

cat > /usr/local/etc/nginx/sites-available/selfdestruct << EOF
server {
    listen 80;
    server_name ${DOMAIN} ${WWW_DOMAIN};

    # Serve Let's Encrypt challenges directly
    location /.well-known/acme-challenge/ {
        root /usr/local/www/nginx;
    }

    # Proxy everything else to self-destruct
    location / {
        proxy_pass http://127.0.0.1:4003;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }
}
EOF

mkdir -p /usr/local/etc/nginx/sites-enabled
mkdir -p /usr/local/www/nginx/.well-known/acme-challenge

chown -R www:www /usr/local/www/nginx
chmod -R 755 /usr/local/www/nginx

ln -sf /usr/local/etc/nginx/sites-available/selfdestruct /usr/local/etc/nginx/sites-enabled/

if ! grep -q "include.*sites-enabled" /usr/local/etc/nginx/nginx.conf; then
    echo "Adding sites-enabled include to nginx.conf..."
    sed -i '' '/http {/a\
    include /usr/local/etc/nginx/sites-enabled/*;' /usr/local/etc/nginx/nginx.conf
else
    echo "sites-enabled already included in nginx.conf"
fi

echo "Nginx configuration created for domains: $DOMAIN and $WWW_DOMAIN"
echo "Site configuration: /usr/local/etc/nginx/sites-available/selfdestruct"
echo "Site enabled: /usr/local/etc/nginx/sites-enabled/selfdestruct"
echo ""
echo "To test and reload Nginx:"
echo "  service nginx configtest"
echo "  service nginx reload"
echo ""
echo "After DNS is pointed at this server, set up SSL with certbot:"
echo "  sudo certbot --nginx -d $DOMAIN -d $WWW_DOMAIN"
