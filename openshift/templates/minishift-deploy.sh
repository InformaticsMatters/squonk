#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

# A simplified `one-click` minishift deployment.
# Derived from the notes in README.md.
#
# Initial conditions are that a set of suitable
# environment variables, defined using `setenv.sh`, have been set.
# i.e. `source setenv.sh` must be run before running this script.
#
# Ths script should be executed from this directory.
#
# Alan Christie
# June 2018

set -e pipefail

./validate.sh
eval $(minishift oc-env)

# The pause between checks during the periods when this script
# waits for Pods to start.
PAUSE_S=30
# Time to wait for 'poster' pods (jobs) to complete...
POSTER_PAUSE_S=5

# The first stage of deployment
# #############################
#
# The series of commands that have to be run in order to get to the SSO
# deployment on MiniShift.

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
sleep $PAUSE_S
TARGET_PODS=3
READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep $PAUSE_S
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
sleep $PAUSE_S
TARGET_PODS=4
READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep $PAUSE_S
    READY_PODS=$(oc get po --no-headers | grep -v "deploy" | grep -v "poster" | grep -v "migrate" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "Squonk is READY"

# The third stage of deployment
# #############################
#
# The series of commands that have to be run in order to get
# the ChemCentral database deployed.

cd chemcentral

oc login -u $OC_ADMIN -p admin
oc project $OC_INFRA_PROJECT > /dev/null
oc create sa chemcentral-postgres
oc adm policy add-scc-to-user anyuid -z chemcentral-postgres
oc process -f chemcentral-pvc-minishift.yaml | oc create -n $OC_INFRA_PROJECT -f -
PG_PVC=$(oc get pvc/chemcentral-postgresql-claim --no-headers | tr -s ' ' | cut -f 3 -d ' ')
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/${PG_PVC}

./deploy.sh

cd ..

echo "Waiting for ChemCentral (infrastructure)..."
sleep $PAUSE_S
oc project $OC_INFRA_PROJECT > /dev/null
TARGET_PODS=4
READY_PODS=$(oc get po --no-headers | grep -v deploy | grep 1/1 | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep $PAUSE_S
    READY_PODS=$(oc get po --no-headers | grep -v deploy | grep 1/1 | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "ChemCentral is READY (infrastructure)"

echo "Waiting for ChemCentral (squonk)..."
oc project $OC_PROJECT > /dev/null
TARGET_PODS=5
READY_PODS=$(oc get po --no-headers | grep -v deploy | grep 1/1 | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS ready)..."
    sleep $PAUSE_S
    READY_PODS=$(oc get po --no-headers | grep -v deploy | grep 1/1 | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "ChemCentral is READY (squonk)"

# The fourth stage - generic pipelines
# ####################################
#
# Deployment of the generic pipelines poster.
# If SQUONK_PIPELINES_PATH is defined we run the deploy.sh script
# from the root of that project. To avoid this do not set
# SQUONK_PIPELINES_PATH.

if [ -z "$SQUONK_PIPELINES_PATH" ]; then
    echo "WARNING: No SQUONK_PIPELINES_PATH set. Skipping pipeline poster deployment."
    exit 0
fi

echo "Deploying Pipelines poster..."
HERE=$PWD
cd $SQUONK_PIPELINES_PATH

./deploy.sh

cd $HERE

echo "Waiting for Pipelines to complete..."
oc project $OC_PROJECT > /dev/null
sleep $POSTER_PAUSE_S
TARGET_PODS=1
READY_PODS=$(oc get po --no-headers | grep pipelines-sd-poster | grep Completed | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $READY_PODS -eq $TARGET_PODS ]
do
    echo "Waiting for $TARGET_PODS pods ($READY_PODS complete)..."
    sleep $POSTER_PAUSE_S
    READY_PODS=$(oc get po --no-headers | grep pipelines-sd-poster | grep Completed | wc -l | tr -s ' ' | cut -f 2 -d ' ')
done
echo "Pipelines is COMPLETE"

# That's all Folks!
# #################

echo "Done"
