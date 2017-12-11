#!/usr/bin/env bash

oc process -f chemcentral.yaml | oc create -f -
echo "Chemcentral database and search service deployed"