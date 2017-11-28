#!/usr/bin/env bash
#

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

# create service account and define permissions
oc create serviceaccount sso-service-account
oc policy add-role-to-user view system:serviceaccount:${OC_INFRA_PROJECT}:sso-service-account


echo "Service account and permissions created. Now you can run './sso-secrets-deploy.sh'"
