#!/usr/bin/env bash
# The environment variable for PRIVATE_HOST will be the address of the docker gateway, something like 172.17.0.1
# The environment variable for PUBLIC_HOST will be the public address, something like squonk.it or the IP of the gateway
# if you are running locally.
# To find out what this is with docker do a:
# docker network inspect deploy_squonk_front
#

export PUBLIC_HOST=172.20.0.1
export PRIVATE_HOST=172.20.0.1

# set the deployment mode:
# dev - for local testing and development
# basic - for more representative setup that uses Keycloak and NGinx
# site - for the full Squonk site including XWiki
export DEPLOYMENT_MODE=dev

if [ $DEPLOYMENT_MODE == 'basic' ]; then
    export COMPOSE_FILE=docker-compose.yml:docker-compose-keycloak.yml:docker-compose-basic.yml
    export SQUONK_URL="http://$PUBLIC_HOST"
    export KEYCLOAK_SERVER_URL=${PUBLIC_HOST_URL}/auth
elif [ $DEPLOYMENT_MODE == 'site' ]; then
    export COMPOSE_FILE=docker-compose.yml:docker-compose-keycloak.yml:docker-compose-site.yml
    export SQUONK_URL="http://$PUBLIC_HOST"
    export KEYCLOAK_SERVER_URL=${PUBLIC_HOST_URL}/auth
elif [ $DEPLOYMENT_MODE == 'dev' ]; then
    export COMPOSE_FILE=docker-compose.yml:docker-compose-dev.yml:docker-compose-setup.yml
    export SQUONK_URL="http://localhost:8080/portal"
    unset KEYCLOAK_SERVER_URL
else
    echo "ERROR: Must define DEPLOYMENT_MODE to be one of basic, site or dev"
    return
fi


export RABBITMQ_ERLANG_COOKIE=topsecret

# password for the admin user of rabbitmq
export RABBITMQ_DEFAULT_PASSWORD=squonk

# password for the squonk user of rabbitmq
export RABBITMQ_SQUONK_PASSWORD=squonk

# username and password for the admin user of keycloak
export KEYCLOAK_USER=admin
export KEYCLOAK_PASSWORD=squonk

# password for the admin user of postgres
export POSTGRES_PASSWORD=squonk

# password for the keycloak user in postgres
export POSTGRES_KEYCLOAK_PASSWORD=squonk

# password for the squonk user in postgres
export POSTGRES_SQUONK_PASSWORD=squonk

# password for the xwik user in postgres
export POSTGRES_XWIKI_PASSWORD=squonk

# The interval (in ms) that Docker service descriptors are reloaded by the coreservices module
# export SQUONK_SERVICE_DISCOVERY_INTERVAL=900000

# The directory that Squonk uses for Docker and nextflow service execution. Needs to be under /squonk/work
#export SQUONK_DOCKER_WORK_DIR=/squonk/work/docker
#export SQUONK_NEXTFLOW_WORK_DIR=/squonk/work/nextflow

# Set to 2 to retain data in $SQUONK_DOCKER_WORK_DIR and SQUONK_NEXTFLOW_WORK_DIR for debugging purposes
#export SQUONK_DEBUG_MODE=0

# The ContainerRunner Type (i.e. 'docker' or 'openshift')
export SQUONK_CONTAINER_RUNNER_TYPE=docker

# The searchable structure databases that have been loaded.
# Must be colon separated table names without any spaces e.g.
# export STRUCTURE_DATABASE_TABLES="emolecules_order_sc:emolecules_order_bb:chembl_23:pdb_ligand"
export STRUCTURE_DATABASE_TABLES="chembl_latest:chembl_23"

# alias docker-compose to dc
alias dc=docker-compose

# generally no need to change these
export PUBLIC_HOST_URL=https://${PUBLIC_HOST}
export SQUONK_DOCKER_SERVICES_DIR=${PWD}/data/docker-services

echo "DEPLOYMENT_MODE is $DEPLOYMENT_MODE"
echo "PUBLIC_HOST is $PUBLIC_HOST"
echo "PRIVATE_HOST is $PRIVATE_HOST"