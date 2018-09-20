#!/bin/bash
#

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

# Create the service account expected by RabbitMQ
oc create sa $OC_INFRA_SA

set +e

# Deploy core RabbitMQ service
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT\
  -P INFRA_SA=$OC_INFRA_SA\
  -p RABBITMQ_HOST=rabbitmq.${OC_INFRA_PROJECT}.svc\
  -f rabbitmq.yaml | oc create -f -
