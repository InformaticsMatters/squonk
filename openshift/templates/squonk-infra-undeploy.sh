#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_PROJECT

set +e

oc adm policy remove-role-from-user edit $OC_USER
oc adm policy remove-scc-from-user anyuid system:serviceaccount:${OC_PROJECT}:default

oc delete all,cm,pvc,secrets --selector template=squonk-infra
oc delete secret/squonk-secrets
oc delete cm/sso-config

echo "You may need to delete persistent volumes"
