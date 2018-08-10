#!/bin/bash

set -e

TAG=$(./images-get-tag.sh)
cd images
echo "building squonk/xwiki:${TAG} docker image ..."
docker build -t squonk/xwiki:${TAG} -f Dockerfile-xwiki .

# finally move back to the original dir
cd ..


