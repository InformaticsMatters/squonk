#!/bin/sh

base=$PWD

cd ../../components/
./gradlew clean

cd $base
./images-build-core.sh
./images-build-portal.sh
./images-build-services.sh

docker push squonk/portal
docker push squonk/chem-services-basic
docker push squonk/core-services-server
docker push squonk/cellexecutor
docker push squonk/xwiki

