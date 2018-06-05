#!/usr/bin/env bash

echo "Deleting chemcentral from $OC_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n $OC_PROJECT
echo "Deleting chemcentral from $OC_INFRA_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n $OC_INFRA_PROJECT
oc delete pvc/chemcentral-postgresql-claim -n $OC_INFRA_PROJECT

echo "Chemcentral database and search service undeployed"