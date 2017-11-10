#!/bin/bash

if [ ! $# == 1 ]; then
    echo "Must specify image tag to push"
    exit 1
fi

echo "Using image tag $1"

set -e

docker push squonk/chemservices-basic:$1
docker push squonk/coreservices:$1
docker push squonk/cellexecutor:$1
docker push squonk/flyway:$1

docker push squonk/portal:latest
docker push squonk/xwiki:latest