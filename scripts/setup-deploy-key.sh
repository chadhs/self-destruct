#!/bin/sh

echo "Setting up SSH deploy key for repository access..."

# This script should be run as the selfdestruct user
if [ "$(whoami)" != "selfdestruct" ]; then
    echo "Error: This script must be run as the selfdestruct user"
    echo "  Run: sudo -u selfdestruct -H sh"
    echo "  Then: ./scripts/setup-deploy-key.sh"
    exit 1
fi

mkdir -p ~/.ssh
chmod 700 ~/.ssh

if [ -f ~/.ssh/id_ed25519 ]; then
    echo "SSH key already exists at ~/.ssh/id_ed25519"
else
    ssh-keygen -t ed25519 -C "selfdestruct-server@$(hostname)" -f ~/.ssh/id_ed25519 -N ""
    echo "SSH key generated."
fi

echo ""
echo "Add this public key as a read-only deploy key in GitHub:"
echo "  Settings > Deploy keys (or repository Settings > Deploy keys)"
echo ""
cat ~/.ssh/id_ed25519.pub
echo ""
echo "After adding the key, test the connection:"
echo "  ssh -T git@github.com"
echo ""
echo "Then clone the repository:"
echo "  git clone git@github.com:chadhs/self-destruct.git /home/selfdestruct/self-destruct"
