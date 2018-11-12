#!/bin/bash

set -e

base=$PWD

export TAG=$(./deploy/images-get-tag.sh)
echo "Using docker tag $TAG"

cd ..
rm -rf docker/docker-services

cd $base

docker-compose stop && docker-compose rm -vf
echo Firing up initial containers
docker-compose up -d postgres rabbitmq stage1

echo Setup up rabbitmq
cd $base && docker exec docker_rabbitmq_1 bash /usr/local/etc/clean.sh

docker-compose up -d --no-recreate flyway

echo Firing up remaining containers
docker-compose up -d --no-recreate chemcentral-search cellexecutor coreservices chemservices stage2



