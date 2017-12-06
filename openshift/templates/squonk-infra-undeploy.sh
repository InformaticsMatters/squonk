#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_PROJECT

set +e

oc adm policy remove-role-from-user edit $OC_USER
oc adm policy remove-scc-from-user anyuid system:serviceaccount:${OC_PROJECT}:default


oc project -q $OC_INFRA_PROJECT

echo "Cleaning up PostgreSQL"
pod=$(oc get pod -o name -l deploymentConfig=postgresql)
echo "Postgresql pod: $pod"
if [ $pod ]; then
  oc rsh $pod /bin/bash -c "psql --command 'drop database squonk;'"
  oc rsh $pod /bin/bash -c "psql --command 'drop role squonk;'"
else
  echo "Postgres pod not found"
fi
oc delete job/squonk-database-creator
oc delete cm/squonk-database-creator
oc delete secret/squonk-database-credentials -n $OC_PROJECT

echo "Cleaning up RabbitMQ"
oc delete job/squonk-rabbitmq-creator
oc delete cm/squonk-rabbitmq-config
oc delete secret/squonk-rabbitmq-credentials -n $OC_PROJECT

echo "Cleaning up Keycloak"
oc delete cm/squonk-sso-config -n $OC_PROJECT
oc delete job/squonk-client-creator cm/squonk-client-creator


echo "You still need to manually delete the squonk client from the Keycloak $KEYCLOAK_REALM realm"
echo "You may need to delete persistent volumes"
