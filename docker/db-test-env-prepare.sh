#!/bin/bash

docker-compose -f docker-compose-db-only.yml rm -vf
docker-compose -f docker-compose-db-only.yml up -d

bash wait-postgres.sh

sleep 6

