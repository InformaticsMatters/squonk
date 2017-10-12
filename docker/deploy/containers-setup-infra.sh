#!/bin/bash

echo "Entering containers-setup-infra.sh"

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

set -e

echo "preparing postgres docker image ..."
docker-compose stop
docker-compose rm -fv postgres rabbitmq stage1
docker-compose up -d postgres rabbitmq stage1

echo "preparing rabbitmq docker image ..."
./images/rabbitmq/rabbitmq-setup.sh deploy_rabbitmq_1
echo "... rabbitmq container configured"
