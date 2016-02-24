# The environment variable for SQUONK_HOST will be something like 172.17.0.1
# To find out what this is with docker do a:
# docker network inspect bridge
#
# variables that are commented out are not required unless changed from the default values 

export SQUONK_HOST=172.17.0.1
export RABBITMQ_ERLANG_COOKIE=topsecret
export RABBITMQ_DEFAULT_PASS=squonk
#export RABBITMQ_SQUONK_PASS=squonk
#export KEYCLOAK_PASSWORD=squonk
export POSTGRES_PASSWORD=squonk
# currently this pasword is shared between the postgres admin and the postgres keycloak users :-(
export POSTGRES_KEYCLOAK_PASS=$POSTGRES_PASSWORD
#export POSTGRES_SQUONK_PASS=squonk
#export POSTGRES_XWIKI_PASS=squonk

# generally no need to change these
export SERVICE_CALLBACK=http://${SQUONK_HOST}/ws/notebook
export KEYCLOAK_SERVER_URL=http://${SQUONK_HOST}:8080
