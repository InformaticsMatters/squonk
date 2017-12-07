#!/usr/bin/env bash

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_USER > /dev/null
oc project -q $OC_PROJECT

set +e

oc delete all,cm,pvc,routes,secrets --selector template=squonk-app