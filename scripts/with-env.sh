#!/bin/sh
# Load .env from the project root, then exec the remaining command.
# Usage: ./scripts/with-env.sh clj -M:migrate

set -e

ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .env ]; then
    echo "Error: .env not found in $ROOT"
    echo "Copy dev.env.example or .env.example to .env and edit values."
    exit 1
fi

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

if [ "$#" -eq 0 ]; then
    echo "Usage: $0 <command> [args...]"
    exit 1
fi

exec "$@"
