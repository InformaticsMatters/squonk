#!/bin/bash 

docker-compose -f docker-compose-db-only.yml stop
docker-compose -f docker-compose-db-only.yml rm -vf
