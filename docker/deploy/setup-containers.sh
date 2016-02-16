#!/bin/sh
#
# builds the docker images that are external to us and should
# only occasionally need rebuilding 

./setenv.sh
base=$PWD

echo "preaparing postgres docker image ..."
docker-compose up -d postgres rabbitmq keycloak

# we need to wait for postgres to start as the next step is to populate the database
attempt=0
until nc -z $(docker inspect --format='{{.NetworkSettings.IPAddress}}' deploy_postgres_1) 5432
do
    if [ $attempt -gt 10 ]; then 
        echo "Giving up on postgres"
	exit 1
    fi
    echo "Waiting for postgres container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done

echo "creating db tables ..."
cd ../../components
./gradlew database:flywayMigrate
echo "... tables created"
cd $base

echo "preparing rabbitmq docker image ..."
./rabbitmq-setup.sh deploy_rabbitmq_1
echo "... rabbitmq container configured"

keycloak_url="http://${DOCKER_GATEWAY}:8080/auth"
#echo "keycloak_url: $keycloak_url"

#docker-compose up -d keycloak
#sleep 5
docker exec deploy_keycloak_1 /opt/jboss/keycloak/bin/add-user.sh -r master -u admin -p ${KEYCLOAK_ADMIN_PASS:-squonk}
echo "admin user added to keycloak"

docker-compose stop keycloak
docker-compose up -d --no-recreate keycloak
echo "keycloak restarted"


attempt=0
until curl -s "${keycloak_url}/realms/master/" > /dev/null
do
    if [ $attempt -gt 20 ]; then 
        echo "Giving up on keycloak"
	exit 1
    fi
    echo "Waiting for keycloak container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done
# no idea why this extra sleep is necessary, but it is
sleep 10


token=$(curl -s -X POST "${keycloak_url}/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=${KEYCLOAK_ADMIN_PASS:-squonk}" -d "grant_type=password" -d "client_id=admin-cli" | jq -r '.access_token')
#echo "token: $token"

curl -s -X POST -T yyy.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"
echo "squonk realm added to keycloak"

docker-compose stop
echo finished
