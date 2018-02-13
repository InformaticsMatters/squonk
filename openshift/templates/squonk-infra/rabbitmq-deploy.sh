#!/bin/bash
#

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

# Claim RabbitMQ PV
oc process -f rabbitmq-pvc.yaml | oc create -f -

# Deploy core RabbitMQ service
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT\
  -p RABBITMQ_HOST=rabbitmq.${OC_INFRA_PROJECT}.svc\
  -f rabbitmq.yaml | oc create -f -
