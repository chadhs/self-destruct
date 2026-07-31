#!/bin/sh
cd /home/selfdestruct/self-destruct || exit 1

# Load environment variables
if [ -f .env ]; then
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
    done < .env
fi

export LANG=en_US.UTF-8
export JAVA_HOME="${JAVA_HOME:-/usr/local/openjdk21}"
export PATH="${JAVA_HOME}/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

if [ ! -x "${JAVA_HOME}/bin/java" ]; then
    echo "Error: Java 21 not found at ${JAVA_HOME}/bin/java"
    exit 1
fi

if ! "${JAVA_HOME}/bin/java" -version 2>&1 | grep -q 'version "21'; then
    echo "Error: ${JAVA_HOME}/bin/java is not Java 21"
    "${JAVA_HOME}/bin/java" -version
    exit 1
fi

PORT="${PORT:-4003}"

# HOST from .env is read by the app via environ (bind to 127.0.0.1 in prod)
exec "${JAVA_HOME}/bin/java" -jar /home/selfdestruct/self-destruct/target/self-destruct.jar --port "${PORT}"
