# The environment variable for DOCKER_GATEWAY will be something like 172.17.0.1
# To find out do a:
# docker network inspect bridge

export DOCKER_GATEWAY=172.17.0.1
export RABBITMQ_DEFAULT_USER=admin
export RABBITMQ_DEFAULT_PASS=squonk
export RABBITMQ_ERLANG_COOKIE=topsecret
export SQUONK_RABBITMQ_VHOST="/squonk"
export SQUONK_RABBITMQ_USER=squonk
export SQUONK_RABBITMQ_PASS=squonk
export SQUONK_SERVICES_CORE=http://${DOCKER_GATEWAY}/coreservices/rest/v1
