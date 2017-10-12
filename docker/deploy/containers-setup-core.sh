#!/bin/bash
#
# sets up the infrastructure containers ready for use.
# once complete setup the squonk containers with ./containers-setup-app.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

set -e

base=$PWD

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# setup postgres and rabbitmq
./containers-setup-infra.sh


if [ ! $DEPLOYMENT_MODE == 'dev' ]; then
    # now setup keycloak
    ./containers-setup-keycloak.sh

    # and now nginx
    ./containers-setup-nginx.sh
fi

echo "Infrastructure containers setup. Now you can run ./containers-setup-app.sh"

