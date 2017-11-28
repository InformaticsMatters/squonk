#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

oc delete secret/sso-jgroup-secret
oc delete secret/sso-ssl-secret


