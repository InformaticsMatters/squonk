#!/usr/bin/env bash

set -e pipefail

../validate.sh

oc login $OC_MASTER_URL -u $OC_USER > /dev/null
oc project -q $OC_PROJECT

oc process -f squonk-app-keycloak.yaml\
  -p SQUONK_HOST=$OC_SQUONK_HOST\
  -p APP_NAMESPACE=$OC_PROJECT\
  -p LOGOUT_REDIRECT_TO=$KEYCLOAK_LOGOUT_REDIRECT_TO\
  -p FLYWAY_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p PORTAL_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p CHEM_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p CORE_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p CELL_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p JE_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  -p SD_POSTER_IMAGE_TAG=$OC_SQUONK_SD_POSTER_IMAGE_TAG\
  | oc create -f -

echo "Squonk deployment is underway. Once complete it can be accessed at http://${OC_SQUONK_HOST}/portal"
