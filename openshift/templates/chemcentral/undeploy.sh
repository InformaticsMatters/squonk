#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

echo "Deleting chemcentral from $OC_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n $OC_PROJECT
echo "Deleting chemcentral from $OC_INFRA_PROJECT project"
oc delete all,cm,secret --selector template=chemcentral -n $OC_INFRA_PROJECT
oc delete pvc/chemcentral-postgresql-claim -n $OC_INFRA_PROJECT

echo "Chemcentral database and search service undeployed"