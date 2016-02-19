#!/bin/sh
#
# builds the docker images that are external to us and should
# only occasionally need rebuilding 

./setenv.sh
base=$PWD

echo "Setting up for server ${DOCKER_GATEWAY}"

echo "preaparing postgres docker image ..."
docker-compose up -d postgres rabbitmq

# we need to wait for postgres to start as the next step is to populate the database
attempt=0
until nc -z $(docker inspect --format='{{.NetworkSettings.IPAddress}}' deploy_postgres_1) 5432
do
    if [ $attempt -gt 10 ]; then 
        echo "Giving up on postgres"
	docker-compose stop
	exit 1
    fi
    echo "Waiting for postgres container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done

# now we can start keycloak (needs postgres to be setup before it starts)
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d keycloak

echo "creating db tables ..."
cd ../../components
./gradlew database:flywayMigrate
echo "... tables created"
cd $base

echo "preparing rabbitmq docker image ..."
./rabbitmq-setup.sh deploy_rabbitmq_1
echo "... rabbitmq container configured"
docker-compose stop rabbitmq

keycloak_url="https://${DOCKER_GATEWAY}:8443/auth"
echo "keycloak_url: $keycloak_url"

# substitute the realm json file
sed "s/192.168.59.103/${DOCKER_GATEWAY}/g" squonk-realm.json > yyy.json


#attempt=0
#until curl -s -k "${keycloak_url}/realms/master/" > /dev/null
#do
#    if [ $attempt -gt 20 ]; then 
#        echo "Giving up on keycloak"
#	 docker-compose stop
#	 exit 1
#    fi
#    echo "Waiting for keycloak container..."
#    sleep 1
#    attempt=$(( $attempt + 1 ))
#done
# no idea why this extra sleep is necessary, but it is
#sleep 2


token=$(curl -s -k -X POST "${keycloak_url}/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=${KEYCLOAK_PASSWORD:-squonk}" -d "grant_type=password" -d "client_id=admin-cli" | jq -r '.access_token')
echo "token: $token"

curl -s -k -X POST -T yyy.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"
echo "squonk realm added to keycloak"

docker-compose stop
echo finished
