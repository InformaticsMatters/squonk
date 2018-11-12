#!/bin/bash

set -e

base=$PWD
echo base = $HOME

TAG=$(./images-get-tag.sh)

touch images/portal/ROOT.war && rm images/portal/*.war

cd ../../../portal/portal-app; ant -f portal.xml build-prod
cd $base
cp ../../../portal/portal-app/dist/portal.war images/portal/ROOT.war

echo "building squonk/portal:${TAG} docker image ..."
cd images
docker build -t squonk/portal:${TAG} -f Dockerfile-portal .
echo "... squonk/portal docker image built"

cd $base
echo finished

