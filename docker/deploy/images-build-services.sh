#!/bin/bash
#
# builds the docker images that are based on our code.
# To be safe run this every time, or just update the containers
# that you know need updating

set -e

base=$PWD

cd ../../components

./gradlew common:assemble common:publish dockerBuildImages

cd $base/images

docker build -f Dockerfile-jobexecutor -t squonk/jobexecutor-keycloak:latest .

cd $base

echo finished
