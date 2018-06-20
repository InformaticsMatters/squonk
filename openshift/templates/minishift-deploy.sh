#!/usr/bin/env bash

set -e pipefail

# The first stage of deployment
# #############################
#
# The series of commands that have to be run in order to get to the SSO
# deployment on MiniShift. Initial conditions here are a set of suitable
# environment variables using `setenv.sh` and so you should have set your
# environment (`source setenv.sh`) before running this script.
#
# Alan Christie

./validate.sh

eval $(minishift oc-env)

oc login -u $OC_USER -p user
oc login -u $OC_ADMIN -p admin

oc login -u system:admin
oc adm policy add-cluster-role-to-user cluster-admin admin

oc login -u $OC_ADMIN
oc create -f https://raw.githubusercontent.com/jboss-openshift/application-templates/master/sso/sso72-image-stream.json -n openshift
oc new-project $OC_PROJECT --display-name='Squonk Applications'
oc new-project $OC_INFRA_PROJECT --display-name='Squonk Infrastructure'

oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -f squonk-infra/infra-pvc-minishift.yaml | oc create -f -
PG_PVC=$(oc get pvc/postgresql-claim --no-headers | tr -s ' ' | cut -f 3 -d ' ')
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/${PG_PVC}

cd squonk-infra
./sso-postgres-deploy.sh
./rabbitmq-deploy.sh
cd ..

oc login -u system:admin
oc volume dc/sso --add --claim-size 512M --mount-path /opt/eap/standalone/configuration/standalone_xml_history --name standalone-xml-history

# Wait for key pod deployments...
# i.e. wait until the following completes...

echo "Waiting for infrastructure..."
TARGET_PODS=3
READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep 10
    READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "Infrastructure is READY"

# The second stage of deployment
# ##############################
#
# The series of commands that have to be run in order to get to the Squonk
# deployment on MiniShift.

cd squonk-app
oc process -p APP_NAMESPACE=$OC_PROJECT -f squonk-pvc-minishift.yaml | oc create -f -
./squonk-infra-deploy.sh
./squonk-app-keycloak-deploy.sh
cd ..

# Wait for key pod deployments...
# i.e. wait until the following completes...

echo "Waiting for Squonk..."
TARGET_PODS=4
READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep 10
    READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "Squonk is READY"

# The third stage of deployment
# #############################
#
# The series of commands that have to be run in order to get to the next
# stage of Squonk deployment on MiniShift (the ChemCentral database).

cd chemcentral

oc login -u $OC_ADMIN -p admin
oc project $OC_INFRA_PROJECT
oc create sa chemcentral-postgres
oc adm policy add-scc-to-user anyuid -z chemcentral-postgres
oc process -f chemcentral-pvc-minishift.yaml | oc create -n $OC_INFRA_PROJECT -f -
PG_PVC=$(oc get pvc/chemcentral-postgresql-claim --no-headers | tr -s ' ' | cut -f 3 -d ' ')
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/${PG_PVC}

./deploy.sh

cd ..