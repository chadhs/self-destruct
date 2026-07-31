#!/bin/sh
set -e

ROOT="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
cd "$ROOT"

export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/self-destruct-test?user=selfdestruct&password=selfdestruct}"
export DATABASE_ENCRYPTION_KEY="${DATABASE_ENCRYPTION_KEY:-changemedbkey2}"
export SESSION_COOKIE_KEY="${SESSION_COOKIE_KEY:-changecookiekey2}"
export DISABLE_ANTI_FORGERY="${DISABLE_ANTI_FORGERY:-true}"
export ENABLE_WORKERS="${ENABLE_WORKERS:-false}"
export PGPASSWORD="${PGPASSWORD:-selfdestruct}"

if psql -h localhost -U selfdestruct -lqt | cut -d \| -f 1 | grep -qw self-destruct-test; then
  dropdb -h localhost -U selfdestruct self-destruct-test
fi

createdb -h localhost -U selfdestruct self-destruct-test
clj -M:migrate
clj -M:test
