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
export PUBLIC_HOST_URL=https://${PUBLIC_HOST}
export KEYCLOAK_SERVER_URL=${PUBLIC_HOST_URL}/auth

export SECRET_KEY=b8ff56a18ee8ce3500d59c083691499aba8f227dddff57c972e1f49a9eff396cc0c83bfa42a78c3cce9f379ef64e9d3c9957572d3af0fe1f432437ce3a1deb5c
