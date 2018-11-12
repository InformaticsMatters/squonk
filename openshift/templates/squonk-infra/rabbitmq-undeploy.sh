#!/usr/bin/env bash

# DEPRECATION NOTICE
#
# YOU SHOULD BE USING THE ANSIBLE PLAYBOOKS in openshift/ansible
# WHERE YOU WILL ALSO FIND A SIMPLE README. ALTHOUGH EVERY ATTEMPT HAS BEEN
# MADE TO KEEP THE SCRIPT YOU SEE HERE IN GOOD ORDER IT MIGHT BE OUT OF DATE.
# IF THE EXISTING ANSIBLE PLAYBOOKS ARE NOT SUITABLE MAKE THEM SO!

set -e

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

set +e

oc delete all,cm,pvc,secrets --selector template=rabbitmq
oc delete sa/$OC_INFRA_SA
