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

# generally no need to change these
export SERVICE_CALLBACK=http://${PRIVATE_HOST}/ws/notebook
export KEYCLOAK_SERVER_URL=http://${PUBLIC_HOST}:8080
export PORTAL_SERVER_URL=http://${PUBLIC_HOST}
