#!/bin/bash

base=$PWD
cd ../components
./gradlew --daemon assemble common:uploadArchives

cd ..
rm -rf docker/docker-services
cp -r data/testfiles/docker-services docker/

cd $base


docker-compose stop
docker-compose rm -vf
docker-compose build
docker-compose up -d chemservices
docker-compose up -d --no-recreate postgres
bash wait-postgres.sh
docker-compose up -d --no-recreate cellexecutor
docker-compose up -d --no-recreate cellexecutor
docker-compose up -d --no-recreate coreservices



