#!/usr/bin/env bash

# Prints a suggested Docker image tag name.
# If the environment variable SQUONK_IMAGE_TAG is set that is returned.
# Otherwise the image tag is the branch name or, if on master, 'latest'
#
# Use this elsewhere with TAG=$(./images-get-tag.sh)

set -e

if [ ! -z "${SQUONK_IMAGE_TAG}" ]; then
    echo ${SQUONK_IMAGE_TAG}
    exit 0
fi

IMAGE_TAG=$(git rev-parse --abbrev-ref HEAD)
if [ IMAGE_TAG == "master" ]; then
  IMAGE_TAG="latest"
fi
echo $IMAGE_TAG
