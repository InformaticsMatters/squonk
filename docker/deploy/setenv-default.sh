# The environment variable for PRIVATE_HOST will be the addres of the docker gateway, something like 172.17.0.1 or localhost
# The environment variable for PUBLIC_HOST will be the public address, something like squonk.it or localhost
# To find out what this is with docker do a:
# docker network inspect bridge
#

export PUBLIC_HOST=172.17.0.1
export PRIVATE_HOST=172.17.0.1
export RABBITMQ_ERLANG_COOKIE=topsecret
export RABBITMQ_DEFAULT_PASS=squonk
export RABBITMQ_SQUONK_PASS=squonk
export KEYCLOAK_PASSWORD=squonk
export POSTGRES_PASSWORD=squonk
# currently this pasword is shared between the postgres admin and the postgres keycloak users :-(
export POSTGRES_KEYCLOAK_PASS=$POSTGRES_PASSWORD
export POSTGRES_SQUONK_PASS=squonk
export POSTGRES_XWIKI_PASS=squonk

# The interval (in ms) that Docker service descriptors are relaoded by the coreservices module
# export SQUONK_SERVICE_DISCOVERY_INTERVAL=900000

# The directory that Squonk uses for Docker service execution. Needs to be under /tmp/work
export SQUONK_DOCKER_WORK_DIR=/tmp/work/squonk

# Set to 2 to retain data in $SQUONK_DOCKER_WORK_DIR for debugging purposes
# export SQUONK_DEBUG_MODE=0

# generally no need to change these
export PUBLIC_HOST_URL=https://${PUBLIC_HOST}
export KEYCLOAK_SERVER_URL=${PUBLIC_HOST_URL}/auth
export SQUONK_DOCKER_SERVICES_DIR=${PWD}/data/docker-services

