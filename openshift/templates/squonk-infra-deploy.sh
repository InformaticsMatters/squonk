#!/usr/bin/env bash

set -e pipefail

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_PROJECT

oc adm policy add-role-to-user edit $OC_USER
oc adm policy add-scc-to-user anyuid system:serviceaccount:${OC_PROJECT}:deployer
oc adm policy add-scc-to-user anyuid system:serviceaccount:${OC_PROJECT}:default

oc login $OC_MASTER_URL -u $OC_USER > /dev/null

oc process -f squonk-secrets.yaml -p SECRETS_NAMESPACE=$OC_PROJECT | oc create -f -
oc process -f squonk-sso-config.yaml\
 -p KEYCLOAK_SERVER_URL=$KEYCLOAK_SERVER_URL\
 -p KEYCLOAK_REALM=$KEYCLOAK_REALM\
 -p KEYCLOAK_SECRET=$KEYCLOAK_SECRET\
 -p SQUONK_APP=$OC_SQUONK_APP\
 -p SECRETS_NAMESPACE=$OC_PROJECT\
 | oc create -f -

oc process -f squonk-infra-openshift.yaml -p INFRA_NAMESPACE=$OC_PROJECT | oc create -f -

echo "You may need to setup persistent volumes before you can deploy"
echo "Infrastructure ready. You can now deploy the Squonk applications using './squonk-app-deploy.sh'"

