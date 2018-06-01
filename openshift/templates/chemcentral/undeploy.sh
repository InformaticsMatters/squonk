#!/usr/bin/env bash

echo "Deleting chemcentral from $OC_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n squonk
echo "Deleting chemcentral from $OC_INFRA_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n squonk-infra
oc delete pvc/chemcentral-postgresql-claim -n squonk-infra

echo "Chemcentral database and search service undeployed"