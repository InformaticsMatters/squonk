#!/bin/bash

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

docker-compose -f docker-compose.yml -f docker-compose-nginx.yml stop
docker-compose -f docker-compose.yml -f docker-compose-nginx.yml up -d --no-recreate 
