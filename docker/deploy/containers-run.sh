#!/bin/bash

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set?"
	exit 1
fi

docker-compose stop
docker-compose up -d --no-recreate
#docker-compose logs
