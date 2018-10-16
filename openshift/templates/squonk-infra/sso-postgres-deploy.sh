#!/usr/bin/env bash
#

set -e pipefail

../validate.sh

oc login $OC_MASTER_URL -u $OC_ADMIN > /dev/null
oc project -q $OC_INFRA_PROJECT

echo "Creating PostgreSQL and Keycloak"
oc process -f sso-postgres.yaml\
 -p SSO_REALM=${KEYCLOAK_REALM}\
 -p POSTGRESQL_SHARED_BUFFERS=1GB\
 -p HOSTNAME_HTTPS=sso.${OC_ROUTES_BASENAME}\
 | oc create -f -

echo "Creating backup Cron Jobs"
oc process -f sso-backup-hourly.yaml | oc create -f -
oc process -f sso-backup-daily.yaml | oc create -f -

echo "PostgreSQL and Keycloak deployed"
