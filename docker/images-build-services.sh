#!/bin/sh
#
# builds the docker images that are based on our code.
# To be safe run this evey time, or just update the containers
# that you know need updating

base=$PWD

cd ../components

#./gradlew --daemon assemble

echo "building chem-services-basic docker image ..."
./gradlew --daemon dockerFileChemServices &&
  docker build -t squonk/chem-services-basic build/chem-services-basic
echo "... chem-services-basic docker image built"


echo "building core-services docker image ..."
./gradlew --daemon core-services-server:dockerFile &&
  docker build -t squonk/core-services-server core-services-server/build
echo "... core-services docker image built"

echo "building cell-executor docker image ..."
./gradlew --daemon cell-executor:dockerBuildImage &&
  docker build -t squonk/cell-executor cell-executor/build/docker
echo "... cell-executor docker image built"


echo finished
