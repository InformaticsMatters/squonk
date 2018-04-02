#!/usr/bin/env bash
#

set -e pipefail

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT


oc process -f sso.yaml\
 -p SSO_REALM=${KEYCLOAK_REALM}\
 -p HTTPS_PASSWORD=${OC_CERTS_PASSWORD}\
 -p JGROUPS_ENCRYPT_PASSWORD=${OC_CERTS_PASSWORD}\
 -p SSO_TRUSTSTORE=truststore.jks\
 -p SSO_TRUSTSTORE_PASSWORD=${OC_CERTS_PASSWORD}\
 -p HOSTNAME_HTTPS=sso.${OC_ROUTES_BASENAME}\
 | oc create -f -

echo "PostgreSQL and Keycloak deployed"

