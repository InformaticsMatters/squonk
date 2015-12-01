#!/bin/bash

HOME=$PWD
echo HOME = $HOME

rm *.war

cd ../../components; ./gradlew core-services-notebook:build
cd $HOME
cp ../../components/core-services-notebook/build/libs/core-services-notebook-0.2-SNAPSHOT.war notebook.war

cd ../../../portal/portal-app; ant -f portal.xml build-portal-app-zip
cd $HOME
cp ../../../portal/portal-app/dist/portal-app.war ROOT.war

