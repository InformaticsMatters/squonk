#!/bin/bash

base=$PWD

docker-compose rm -vf && docker-compose -f docker-compose-db-only.yml up -d postgres stage1 || exit 1

echo creating db tables
cd ../components && ./gradlew assemble database:flywayMigrate || exit 1
cd $base



