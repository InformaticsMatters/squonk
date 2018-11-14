#!/bin/bash

set -e

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

base=$PWD

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

docker-compose stop

if [ ! $DEPLOYMENT_MODE == 'dev' ]; then

    # setup nginx
    ./containers-setup-nginx.sh

    # setup keycloak
    ./containers-setup-keycloak.sh
fi

echo "preparing postgres docker image ..."
docker-compose rm -fv postgres rabbitmq stage1
docker-compose up -d postgres rabbitmq stage1

echo "preparing rabbitmq docker image ..."
./images/rabbitmq/rabbitmq-setup.sh deploy_rabbitmq_1
echo "... rabbitmq container configured"

echo "Infrastructure containers setup. Now you can run ./containers-setup-app.sh"