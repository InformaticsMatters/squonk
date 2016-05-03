#!/bin/bash

base=$PWD
echo base = $HOME

touch images/portal/ROOT.war && rm images/portal/*.war

cd ../../../portal/portal-app; ant -f portal.xml build-prod
cd $base
cp ../../../portal/portal-app/dist/portal.war images/portal/ROOT.war

echo "building squonk/portal docker image ..."
cd images
docker build -t squonk/portal -f Dockerfile-portal .
echo "... squonk/portal docker image built"

cd $base
echo finished

