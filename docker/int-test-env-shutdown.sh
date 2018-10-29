#!/bin/bash 

export TAG=$(./deploy/images-get-tag.sh)

docker-compose stop
