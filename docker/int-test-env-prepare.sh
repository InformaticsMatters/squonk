#!/bin/bash

base=$PWD
cd ../components
./gradlew --daemon assemble common:uploadArchives
cd $base

#docker build -t 'squonk/groovy' deploy/images/groovy

docker-compose stop
docker-compose rm -vf
docker-compose build
docker-compose up -d

bash wait-postgres.sh


