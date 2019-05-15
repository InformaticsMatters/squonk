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

cd $base

echo finished
