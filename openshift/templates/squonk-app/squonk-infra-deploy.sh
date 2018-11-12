#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

set -e pipefail

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null

oc project -q $OC_INFRA_PROJECT

echo "Preparing PostgreSQL"
oc process\
  -p SQUONK_NAMESPACE=$OC_PROJECT\
  -p DATABASE_HOST=${OC_POSTGRESQL_SERVICE}.${OC_INFRA_PROJECT}.svc\
  -f squonk-infra-db-init.yaml\
  | oc create -f -

echo "Preparing RabbitMQ"
oc process\
  -p SQUONK_NAMESPACE=$OC_PROJECT\
  -p RABBITMQ_HOST=rabbitmq.${OC_INFRA_PROJECT}.svc\
  -f squonk-infra-rabbitmq-init.yaml\
  | oc create -f -

echo "Preparing Keycloak (Client)"
oc process -f squonk-infra-keycloak-init.yaml \
  -p KEYCLOAK_REALM=$KEYCLOAK_REALM\
  -p ROUTES_BASE_HOSTNAME=$OC_ROUTES_BASENAME \
  -p LOGOUT_REDIRECT_TO=$KEYCLOAK_LOGOUT_REDIRECT_TO\
  | oc create -f -

oc project -q $OC_PROJECT

echo "Preparing roles and service accounts"
oc adm policy add-role-to-user edit $OC_USER
oc adm policy add-scc-to-user anyuid system:serviceaccount:${OC_PROJECT}:default
oc adm policy add-cluster-role-to-user cluster-admin -z default

echo "You may need to setup persistent volumes before you can deploy"
echo "Keycloak client creation initiated. Check the output by running 'oc logs job/squonk-client-creator -n $OC_INFRA_PROJECT'"
echo "Infrastructure ready. You can now deploy the Squonk applications using './squonk-app-keycloak-deploy.sh'"
