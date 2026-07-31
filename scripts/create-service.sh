#!/bin/sh

echo "Creating FreeBSD rc.d service script..."

# Check if running as root
if [ "$(id -u)" != "0" ]; then
    echo "Error: This script must be run as root (use sudo)"
    exit 1
fi

APP_PATH="/home/selfdestruct/self-destruct"

if [ ! -d "$APP_PATH" ]; then
    echo "Error: Application directory not found: $APP_PATH"
    echo "  Make sure the self-destruct application is deployed first"
    exit 1
fi

echo "Creating service script..."

if [ -f "$APP_PATH/scripts/selfdestruct.rc.template" ]; then
    cp "$APP_PATH/scripts/selfdestruct.rc.template" /usr/local/etc/rc.d/selfdestruct
else
    echo "Error: rc.d template not found: $APP_PATH/scripts/selfdestruct.rc.template"
    exit 1
fi

chmod +x /usr/local/etc/rc.d/selfdestruct

touch /var/log/selfdestruct.log
chown selfdestruct:selfdestruct /var/log/selfdestruct.log
chmod 644 /var/log/selfdestruct.log

echo "Service script created: /usr/local/etc/rc.d/selfdestruct"
echo "Log file created: /var/log/selfdestruct.log"
echo ""
echo "To enable and start the service:"
echo "  sysrc selfdestruct_enable=\"YES\""
echo "  service selfdestruct start"
echo ""
echo "To check service status:"
echo "  service selfdestruct status"
