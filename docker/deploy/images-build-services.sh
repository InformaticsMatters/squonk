#!/bin/bash
#
# builds the docker images that are based on our code.
# To be safe run this every time, or just update the containers
# that you know need updating

base=$PWD

cd ../../components

./gradlew --daemon common:assemble common:publish buildDockerImages

cd $base

echo finished
