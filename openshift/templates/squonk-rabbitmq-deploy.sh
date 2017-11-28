#!/usr/bin/env bash

set -e pipefail

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

oc process\
  -p SQUONK_NAMESPACE=$OC_PROJECT\
  -p RABBITMQ_HOST=rabbitmq.${OC_INFRA_PROJECT}.svc\
  -f squonk-rabbitmq-init.yaml\
  | oc create -f -
