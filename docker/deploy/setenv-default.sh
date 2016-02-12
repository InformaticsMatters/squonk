# The environment variable for DOCKER_GATEWAY will be something like 172.17.0.1
# To find out do a:
# docker network inspect bridge
#
# variables that are commented out are not required unless changed from the default values 

export DOCKER_GATEWAY=172.17.0.1
export RABBITMQ_ERLANG_COOKIE=topsecret
export RABBITMQ_DEFAULT_PASS=squonk
#export KEYCLOAK_ADMIN_PASS=squonk
export POSTGRES_PASSWORD=squonk
#export POSTGRES_KEYCLOAK_PASS=squonk
#export POSTGRES_SQUONK_PASS=squonk
#export POSTGRES_XWIKI_PASS=squonk

# generally no need to change these
export SERVICE_CALLBACK=http://${DOCKER_GATEWAY}/ws/callback
export KEYCLOAK_SERVER_URL=http://${DOCKER_GATEWAY}:8080
