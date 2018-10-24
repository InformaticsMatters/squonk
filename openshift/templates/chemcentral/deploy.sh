#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

echo "Deploying ChemCentral"
oc process -f chemcentral-db.yaml -p DB_NAMESPACE=$OC_INFRA_PROJECT -p APP_NAMESPACE=$OC_PROJECT | oc create -f -
oc process -f chemcentral-search.yaml -p APP_NAMESPACE=$OC_PROJECT | oc create -f -

echo "Chemcentral database and search service deployed"