#!/bin/sh
#
# builds the docker images that are based on our code.
# To be safe run this evey time, or just update the containers
# that you know need updating

base=$PWD

cd ../components

echo "building chem-services-basic docker image ..."
./gradlew --daemon dockerFileChemServices &&
  docker build -t squonk/chem-services-basic build/chem-services-basic
echo "... chem-services-basic docker image built"


echo "building core-services docker image ..."
./gradlew --daemon core-services-server:dockerFile &&
  docker build -t squonk/core-services-server core-services-server/build
echo "... core-services docker image built"

echo "building core-services-notebook docker image ..."
./gradlew --daemon core-services-notebook:dockerFile &&
  docker build -t squonk/core-services-notebook core-services-notebook/build
echo "... core-services-notebook docker image built"


echo finished
