#!/bin/bash
#
# sets up the containers ready for use.
# once complete run them with run-containers.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

set -e

base=$PWD

if [ ! -d /tmp/work ]; then
  mkdir /tmp/work
fi

if [ ! -f images/chemservices/license.cxl ]; then
	echo "Creating default ChemAxon license file"
	cp images/chemservices/license-empty.cxl images/chemservices/license.cxl
fi

if [ ! -f images/cellexecutor/logging.properties ]; then
	echo "Creating default logging config for cellexecutor"
	cp images/cellexecutor/logging.properties.default images/cellexecutor/logging.properties
fi

if [ ! -f images/portal/logging.properties ]; then
	echo "Creating default logging config for portal"
	cp images/portal/logging.properties.default images/portal/logging.properties
fi

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# set up the proxy details in the tomcat apps 
sed "s/__public_host__/${PUBLIC_HOST}/g" images/portal/server.xml.template > images/portal/server.xml

docker-compose stop chemservices coreservices cellexecutor chemcentral-search
docker-compose rm -fv chemservices coreservices cellexecutor chemcentral-search

docker-compose up -d --no-recreate

echo "Setup complete. Access squonk at $SQUONK_URL"
