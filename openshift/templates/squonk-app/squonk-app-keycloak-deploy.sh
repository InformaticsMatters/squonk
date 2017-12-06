#!/usr/bin/env bash

set -e pipefail

../validate.sh

oc login $OC_MASTER_URL -u $OC_USER > /dev/null
oc project -q $OC_PROJECT

oc process -f squonk-app-keycloak.yaml \
  -p SQUONK_HOST=${OC_SQUONK_HOST}\
  -p APP_NAMESPACE=$OC_PROJECT\
  | oc create -f -

echo "Squonk deployment is underway. Once complete it can be accessed at http://${OC_SQUONK_HOST}/portal"
