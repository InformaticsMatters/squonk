#!/bin/sh
#
# builds the docker images that are based on our code.
# To be safe run this evey time, or just update the containers
# that you know need updating

base=$PWD

cd ../components

echo "building core-services docker image ..."
./gradlew --daemon core-services-server:dockerFile &&
  docker build -t squonk/core-services core-services-server/build
echo "... core-services docker image built"


echo finished
