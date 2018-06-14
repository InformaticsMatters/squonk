#!/usr/bin/env bash

set -e pipefail

# The second stage of deployment.
#
# The series of commands that have to be run in order to get to the Squonk
# deployment on MiniShift. You must have run the first stage before you
# run this stage.
#
# Alan Christie

./validate.sh

eval $(minishift oc-env)

cd squonk-app
oc process -p APP_NAMESPACE=$OC_PROJECT -f squonk-pvc-minishift.yaml | oc create -f -

./squonk-infra-deploy.sh
./squonk-app-keycloak-deploy.sh

# Wait for key pod deployments...
# i.e. wait until the following completes...

TARGET_PODS=4
READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS ready pods ($READY_PODS are ready)..."
    sleep 8
    READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "Ready"
