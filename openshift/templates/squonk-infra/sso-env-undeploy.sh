#!/usr/bin/env bash
#

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +x

# delete service account and permissions
oc policy remove-role-from-user view system:serviceaccount:${OC_INFRA_PROJECT}:sso-service-account
oc delete serviceaccount/sso-service-account

oc delete secret/sso-jgroup-secret
oc delete secret/sso-ssl-secret
