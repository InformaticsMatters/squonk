#!/usr/bin/env bash

set -e

./validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

oc secret new sso-jgroup-secret certs/jgroups.jceks
oc secret new sso-ssl-secret certs/sso-https.jks certs/truststore.jks
oc secrets link sso-service-account sso-jgroup-secret sso-ssl-secret

echo "Keycloak secrets deployed. Now you can run './sso-deploy.sh'"
