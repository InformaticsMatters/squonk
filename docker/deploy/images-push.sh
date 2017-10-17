#!/bin/bash

base=$PWD

./images-build-all.sh

docker push squonk/portal
docker push squonk/chem-services-basic
docker push squonk/coreserver
docker push squonk/cellexecutor
docker push squonk/xwiki
docker push squonk/flyway

