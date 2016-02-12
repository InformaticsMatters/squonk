#!/bin/sh
#
# builds the docker images that are external to us and should
# only occasionally need rebuilding 

./setenv.sh



base=$PWD

echo "preparing rabbitmq docker image ..."
docker-compose up -d rabbitmq
sleep 2
docker exec deploy_rabbitmq_1 bash /usr/local/etc/init.sh
docker-compose stop rabbitmq
echo "... rabbitmq docker image built"

echo "preaparing postgres docker image ..."
docker-compose up -d postgres
sleep 2
cd ../../components
./gradlew database:flywayMigrate
echo "... tables created"
cd $base

docker-compose up -d keycloak
sleep 5
docker exec deploy_keycloak_1 /opt/jboss/keycloak/bin/add-user.sh -r master -u admin -p ${KEYCLOAK_ADMIN_PASS:-squonk}
docker-compose stop keycloak

docker run -it --link deploy_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=${POSTGRES_KEYCLOAK_PASS:-keycloak} --rm -v $PWD:/tmp/json jboss/keycloak-postgres:1.8.1.Final -b 0.0.0.0 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/yyy.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING




echo finished
