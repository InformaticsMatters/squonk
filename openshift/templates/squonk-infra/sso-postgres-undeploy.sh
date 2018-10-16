#!/usr/bin/env bash
#

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

oc delete all -l application=sso
oc delete secret/keycloak-secrets
oc delete secret/postgresql-secrets
oc delete cronjob --selector template=sso-backup

echo "The PVC used by PostgreSQL has not been deleted. Remove this manually if you need."
