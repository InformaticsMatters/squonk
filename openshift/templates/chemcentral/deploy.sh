#!/usr/bin/env bash


echo "Deploying ChemCentral"
oc process -f chemcentral.yaml -p DB_NAMESPACE=$OC_INFRA_PROJECT -p APP_NAMESPACE=$OC_PROJECT | oc create -f -

echo "Chemcentral database and search service deployed"