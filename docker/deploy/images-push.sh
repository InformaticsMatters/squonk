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
docker push squonk/jobexecutor:$1
docker push squonk/jobexecutor-keycloak:$1
docker push squonk/flyway:$1

docker push squonk/chemcentral-search:$1
docker push squonk/chemcentral-loader:$1

docker push squonk/portal:$1
