#!/bin/sh
set -e

export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/self-destruct-test?user=selfdestruct&password=selfdestruct}"

if psql -lqt | cut -d \| -f 1 | grep -qw self-destruct-test; then
  dropdb self-destruct-test
fi

createdb self-destruct-test
lein migrate
lein with-profile test test
