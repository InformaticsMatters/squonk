#!/usr/bin/env bash

set -e pipefail

# The third stage of deployment.
#
# The series of commands that have to be run in order to get to the next
# stage of Squonk deployment on MiniShift (the ChemCentral database).
# You must have run the second stage before you run this stage.
#
# Alan Christie

./validate.sh

eval $(minishift oc-env)

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
