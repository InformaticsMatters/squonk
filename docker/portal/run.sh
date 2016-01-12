#!/bin/bash

docker-compose stop
docker-compose rm -f
./build.sh
docker-compose build
docker-compose up -d


