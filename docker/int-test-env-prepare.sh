#!/bin/bash

set -e

base=$PWD

cd ..
rm -rf docker/docker-services
cp -r data/testfiles/docker-services docker/

cd $base

docker-compose stop && docker-compose rm -vf
echo Firing up initial containers
docker-compose up -d postgres rabbitmq chemservices stage1

echo Creating db tables
cd ../components && ./gradlew database:flywayMigrate

echo Setup up rabbitmq
cd $base && docker exec docker_rabbitmq_1 bash /usr/local/etc/clean.sh

echo Firing up remaining containers
docker-compose up -d --no-recreate cellexecutor coreservices stage2



