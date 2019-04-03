#!/bin/bash
#
# builds the docker images that are based on our code.
# To be safe run this every time, or just update the containers
# that you know need updating

set -e

base=$PWD

TAG=$(./images-get-tag.sh)

cd ../../components

./gradlew common:assemble common:publish dockerBuildImages

cd $base/images

sed "s/__TAG__/$TAG/g" Dockerfile-jobexecutor-template > Dockerfile-jobexecutor

echo "building squonk/jobexecutor-keycloak:${TAG} docker image ..."
docker build -f Dockerfile-jobexecutor -t squonk/jobexecutor-keycloak:${TAG} .

cd $base

echo finished
