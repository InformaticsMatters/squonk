#!/bin/bash

base=$PWD
export TAG=$(./deploy/images-get-tag.sh)

docker-compose rm -vf && docker-compose up -d postgres stage1 || exit 1

echo creating db tables
docker-compose up -d --no-recreate flyway

cd $base



