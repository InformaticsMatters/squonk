#!/bin/bash
#
# builds the docker images that are based on our code.
# To be safe run this every time, or just update the containers
# that you know need updating

base=$PWD

cd ../../components

./gradlew --daemon common:assemble common:publish

#docker rmi squonk/core-services-server squonk/chem-services-basic squonk/cellexecutor

echo "building chem-services-basic docker image ..."
./gradlew dockerFileChemServices &&
  docker build -t squonk/chem-services-basic build/chem-services-basic
echo "... chem-services-basic docker image built"

echo "building core-services docker image ..."
./gradlew core-services-server:buildDockerFile &&
  docker build -t squonk/core-services-server core-services-server/build
echo "... core-services docker image built"

echo "building cell-executor docker image ..."
./gradlew cell-executor:dockerBuildImage &&
  docker build -t squonk/cellexecutor cell-executor/build/docker
echo "... cell-executor docker image built"

cd $base

echo finished
