#!/bin/sh
#
# builds the core docker images that are not based on our code.
# Most of these use images from Docker hub

echo "building squonk/groovy docker image ..."
docker build -t 'squonk/groovy' images/groovy
