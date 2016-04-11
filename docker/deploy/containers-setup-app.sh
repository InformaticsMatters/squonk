#!/bin/sh
#
# sets up the containers ready for use.
# once comple run them with run-containers.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

base=$PWD

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# set up the proxy details in the tomcat apps 
sed "s/__public_host__/${PUBLIC_HOST}/g" portal/server.xml.template > portal/server.xml

images="chemservices coreservices cellexecutor portal nginx"

docker-compose stop $images
docker-compose rm -f $images
docker-compose build $images


docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres

# we need to wait for postgres to start as the next step is to populate the database
attempt=0
until nc -z -w 1 $PRIVATE_HOST 5432
do
    if [ $attempt -gt 10 ]; then 
        echo "Giving up on postgres"
	    docker-compose stop
	exit 1
    fi
    echo "waiting for postgres container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done
echo "postgres is up"


echo "creating db tables ..."
cd ../../components
SQUONK_DB_SERVER=$PRIVATE_HOST
./gradlew database:flywayMigrate
echo "... tables created"
cd $base


docker-compose stop
echo finished
