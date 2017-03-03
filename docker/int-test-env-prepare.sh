#!/bin/bash

base=$PWD
cd ../components
#./gradlew assemble common:uploadArchives

cd ..
rm -rf docker/docker-services
cp -r data/testfiles/docker-services docker/

cd $base

docker-compose stop && docker-compose rm -vf && docker-compose build || exit 1
echo Firing up inital containers
docker-compose up -d postgres rabbitmq chemservices stage1 || exit 1

echo Creating db tables
cd ../components && ./gradlew assemble database:flywayMigrate || exit 1

echo Setup up rabbitmq
cd $base && docker exec docker_rabbitmq_1 bash /usr/local/etc/clean.sh || exit 1

echo Firing up remaining containers
docker-compose up -d --no-recreate cellexecutor coreservices stage2 || exit 1



