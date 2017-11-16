#!/usr/bin/env bash

echo "Upgrading squonk database at ${POSTGRES_HOSTNAME:-postgres}"

./flyway -password=$POSTGRES_PASSWORD -url="jdbc:postgresql://${POSTGRES_HOSTNAME:-postgres}/squonk" migrate
echo "Upgrade complete"