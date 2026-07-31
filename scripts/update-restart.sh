#!/bin/sh

## update
sudo -u selfdestruct sh -c 'cd /home/selfdestruct/self-destruct && git pull'
if [ $? -ne 0 ]; then
    echo "Error: git pull failed, aborting deployment"
    exit 1
fi
## build and restart if successful
sudo -u selfdestruct sh -c 'cd /home/selfdestruct/self-destruct && ./scripts/build-prod.sh' && sudo service selfdestruct stop && sudo service selfdestruct start
## clean exit
exit 0
