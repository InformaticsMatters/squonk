#!/bin/bash

base=$PWD

cd ../../components/
./gradlew clean database:buildDockerImage

cd $base
./images-build-core.sh
./images-build-portal.sh
./images-build-services.sh

docker push squonk/portal
docker push squonk/chemservices-basic
docker push squonk/core-services-server
docker push squonk/cellexecutor
docker push squonk/xwiki
docker push squonk/flyway

