#!/bin/bash

docker-compose stop
docker-compose rm -f
cd ..
./build-services.sh
cd deploy
./build-portal.sh
docker-compose build
./setup-containers.sh
docker-compose up -d
docker-compose logs
