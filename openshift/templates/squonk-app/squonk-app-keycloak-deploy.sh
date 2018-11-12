#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

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
  | oc create -f -

oc process -f squonk-app-keycloak-je.yaml\
  -p APP_NAMESPACE=$OC_PROJECT\
  -p JE_IMAGE_TAG=$OC_SQUONK_IMAGE_TAG\
  | oc create -f -

oc process -f squonk-app-je-route.yaml\
  -p APP_NAMESPACE=$OC_PROJECT\
  -p JE_HOST=$OC_JOBEXECUTOR_HOST\
  | oc create -f -

echo "Squonk deployment is underway. Once complete it can be accessed at http://${OC_SQUONK_HOST}/portal"
