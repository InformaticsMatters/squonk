#!/bin/bash

export PGUSER=postgres
export PGPASSWORD=${POSTGRES_PASSWORD-postgres}

echo "Setting up db as user ${PGUSER}"

psql --command "CREATE USER chemcentral WITH PASSWORD 'chemcentral';"
echo "created chemcentral user with password chemcentral"

createdb -O chemcentral chemcentral
echo "Created chemcentral database"
psql --command 'create extension rdkit' chemcentral
echo "RDKit cartridge installed"
psql -d chemcentral --command "CREATE SCHEMA vendordbs AUTHORIZATION chemcentral;"
echo "vendordbs schema created"
