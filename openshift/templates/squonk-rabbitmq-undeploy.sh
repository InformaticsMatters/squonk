#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

oc delete job/squonk-rabbitmq-creator
oc delete cm/squonk-rabbitmq-config
oc delete secret/squonk-rabbitmq-credentials -n $OC_PROJECT
