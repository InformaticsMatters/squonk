#!/usr/bin/env bash

set -e

echo "Upgrading squonk database at ${POSTGRES_HOSTNAME:-postgres}"

./flyway \
  -user=${POSTGRES_USER:-squonk} \
  -password=$POSTGRES_PASSWORD \
  -url="jdbc:postgresql://${POSTGRES_HOSTNAME:-postgres}/${POSTGRES_DATABASE:-squonk}" \
  -validateOnMigrate=false \
  migrate

echo "Upgrade complete"