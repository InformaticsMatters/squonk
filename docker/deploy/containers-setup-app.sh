#!/bin/sh
#
# sets up the containers ready for use.
# once complete run them with run-containers.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

base=$PWD

if [ ! -d /tmp/work ]; then
  mkdir /tmp/work
fi

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# set up the proxy details in the tomcat apps 
sed "s/__public_host__/${PUBLIC_HOST}/g" images/portal/server.xml.template > images/portal/server.xml
sed "s/__POSTGRES_SQUONK_PASSWORD__/${POSTGRES_SQUONK_PASSWORD}/g" images/portal/persistence.properties.template > images/portal/persistence.properties


docker-compose rm -fv portal chemservices coreservices cellexecutor
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d --no-recreate postgres rabbitmq stage1 || exit 1


echo "updating db tables ..."
cd ../../components
SQUONK_DB_SERVER=$PRIVATE_HOST
./gradlew database:flywayMigrate || exit 1
echo "... tables created"
cd $base

docker-compose stop

echo finished
