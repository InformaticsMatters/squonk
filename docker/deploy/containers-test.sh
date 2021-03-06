#!/bin/bash

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

docker-compose stop
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d --no-recreate
