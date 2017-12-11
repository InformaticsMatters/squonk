#!/usr/bin/env bash

oc delete all,cm,pvc,secrets --selector template=chemcentral
echo "Chemcentral database and search service undeployed"