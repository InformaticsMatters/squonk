#!/bin/sh
#
# builds the docker images that are external to us and should
# only occasionally need rebuilding 

echo "building postgres docker image ..."
docker build -t squonk/postgres postgres/
echo "... postgres docker image built"


echo "building rabbitmq docker image ..."
docker build -t squonk/rabbitmq rabbitmq/
echo "... rabbitmq docker image built"


echo finished
