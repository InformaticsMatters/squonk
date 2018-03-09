#!/bin/bash

base=$PWD

./images-build-all.sh

docker push squonk/portal:latest
docker push squonk/chemservices-basic:latest
docker push squonk/coreservices:latest
docker push squonk/cellexecutor:latest
docker push squonk/xwiki:latest
docker push squonk/flyway:latest

