#!/bin/sh
#
# builds the docker images that are based on our code.
# To be safe run this evey time, or just update the containers
# that you know need updating

base=$PWD

cd ../components

echo "building cell-executor docker image ..."
./gradlew --daemon cell-executor:dockerBuildImage 
#&&
#  docker build -t squonk/cell-executor cell-executor/build
echo "... cell-executor docker image built"

echo finished
