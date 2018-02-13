#!/usr/bin/env bash
#

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

oc delete all -l application=sso
oc delete secret/sso
oc delete secret/postgresql

oc delete pvc/postgresql-claim
