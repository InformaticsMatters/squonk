#!/bin/sh
#
# builds the docker images that are external to us and should
# only occasionally need rebuilding 

./setenv.sh



base=$PWD

echo "preparing rabbitmq docker image ..."
docker-compose up -d mq
sleep 2
docker exec docker_mq_1 bash /usr/local/etc/init.sh
docker-compose stop mq
echo "... rabbitmq docker image built"

echo finished
