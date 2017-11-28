#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

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
