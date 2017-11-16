#!/usr/bin/env bash

echo "Upgrading squonk database at ${POSTRES_HOSTNAME:-postgres}"

./flyway -password=$POSTGRES_PASSWORD -url="jdbc:postgresql://${POSTRES_HOSTNAME:-postgres}/squonk" migrate
echo "Upgrade complete"