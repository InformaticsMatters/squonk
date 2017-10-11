#!/usr/bin/env bash

echo "Upgrading squonk database"
./flyway -password=$POSTGRES_PASSWORD migrate
echo "Upgrade complete"