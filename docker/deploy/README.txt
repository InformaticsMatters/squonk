To setup:
=========

create a self-signed certificate for keycloak and put it in the keycloak dir. see:
http://keycloak.github.io/docs/userguide/keycloak-server/html/server-installation.html#d4e345


cp setenv-default.sh  setenv.sh    # create the file that defines the environment variables
# edit setenv.sh as needed changing passwords and docker gateway address
source setenv.sh                   # to set the environment variables

./images-build-all.sh              # build all docker images
./containers-setup.sh              # one-off setup and configuration
./containers-run.sh                # start the containers




To run:
=======

docker-compose up -d --no-recreate
or if all containers have been created
docker-compose start


To export the real configuration:
=================================
docker run -it --link deploy_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=squonk --rm -v $PWD:/tmp/json jboss/keycloak-postgres:1.9.1.Final -b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-191.json -Dkeycloak.migration.realmName=squonk


To build a new environment:
===========================


Ubuntu 15.10 image
Install openjdk-8-jdk ant jq
Install docker-engine (https://docs.docker.com/compose/install/)
Install docker compose (https://docs.docker.com/compose/install/)

pull the lac and portal repos
build as above (remember to create/edit setenv.sh)





