#!/bin/sh

[[ -n $(psql -l | grep self-destruct-test) ]] && dropdb self-destruct-test
createdb self-destruct-test
lein with-profile test migrate && lein test
