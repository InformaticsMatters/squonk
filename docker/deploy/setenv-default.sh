# The environment variable for DOCKER_GATEWAY will be something like 172.17.0.1
# To find out do a:
# docker network inspect bridge

export DOCKER_GATEWAY=172.17.0.1
export RABBITMQ_ERLANG_COOKIE=topsecret
export RABBITMQ_DEFAULT_USER=admin
export RABBITMQ_DEFAULT_PASS=squonk
export SQUONK_SERVICES_CORE=http://${DOCKER_GATEWAY}:8090/coreservices/rest/v1
export SERVICE_CALLBACK=http://${DOCKER_GATEWAY}/ws/callback
export KEYCLOAK_SERVER_URL=http://${DOCKER_GATEWAY}:8080
