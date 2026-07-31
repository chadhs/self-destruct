#!/bin/sh

echo "Creating production environment file..."

# Check if we're in the right directory
if [ ! -f "project.clj" ]; then
    echo "Error: Please run this script from the self-destruct project root directory"
    exit 1
fi

echo "Please provide the following information:"

printf "Database password for 'selfdestruct' user: "
stty -echo
read DB_PASSWORD
stty echo
echo

printf "Domain name (e.g., selfdestruct.example.com): "
read DOMAIN

printf "App port [4003]: "
read PORT
PORT=${PORT:-4003}

# Generate secrets
echo "Generating secrets..."
SESSION_COOKIE_KEY=$(openssl rand -hex 8)
DATABASE_ENCRYPTION_KEY=$(openssl rand -hex 32)

DB_NAME="selfdestruct_prod"
DB_USER="selfdestruct"
DB_HOST="localhost"
DB_PORT="5432"
DATABASE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}"

cat > .env << EOF
# Database Configuration
DATABASE_URL=${DATABASE_URL}
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_NAME=${DB_NAME}
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASSWORD}

# Server Configuration
PORT=${PORT}
HOST=127.0.0.1

# Secrets
DATABASE_ENCRYPTION_KEY=${DATABASE_ENCRYPTION_KEY}
SESSION_COOKIE_KEY=${SESSION_COOKIE_KEY}

# Workers
ENABLE_WORKERS=true
MESSAGE_EXPIRE_MINUTES=1440
WORKER_DELAY_SECONDS=3600

# Logging
REPORTED_LOG_LEVEL=warn
LOG_APPENDER=println

# Optional: Ring secure-site-defaults when terminating TLS at nginx
# SECURE_DEFAULTS=true

# Optional reference (not read by the app)
APP_BASE_URL=https://${DOMAIN}
EOF

chmod 600 .env

echo "Environment file created: .env"
echo "File permissions set to 600 (owner read/write only)"
echo ""
echo "Generated SESSION_COOKIE_KEY and DATABASE_ENCRYPTION_KEY."
echo "You can edit .env manually to adjust logging or worker settings."
