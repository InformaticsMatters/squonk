#!/bin/bash

# for openshift the public hostname master
# for minishift the IP address of the minishift VM. eg. `minishift ip`  
export OC_MASTER_HOSTNAME=example.org
export OC_CERTS_PASSWORD=changeme
export OC_DEPLOYMENT=squonk
export OC_ADMIN=admin
export OC_USER=developer
export OC_MASTER_URL=https://${OC_MASTER_HOSTNAME}:8443
# for openshift change this to the hostname of the infra node hosting the router
export OC_ROUTES_BASENAME=${OC_MASTER_HOSTNAME}
export KEYCLOAK_SERVER_URL=https://sso.${OC_ROUTES_BASENAME}/auth
export KEYCLOAK_REALM=squonk
export KEYCLOAK_LOGOUT_REDIRECT_TO=http://home.${OC_ROUTES_BASENAME}/
export OC_INFRA_PROJECT=squonk-infra
export OC_PROJECT=squonk
export OC_SQUONK_APP=squonk-notebook
export OC_SQUONK_HOST=${OC_SQUONK_APP}.${OC_ROUTES_BASENAME}
export OC_DOMAIN_NAME=novalocal
export OC_NFS_SERVER=xchem-infra.$OC_DOMAIN_NAME
export OC_OPENSHIFT_VERSION=3.7



echo "OC_PROJECT set to $OC_PROJECT"
echo "OC_INFRA_PROJECT set to $OC_INFRA_PROJECT"
echo "OC_MASTER_HOSTNAME set to $OC_MASTER_HOSTNAME"
echo "OC_ROUTES_BASENAME set to $OC_ROUTES_BASENAME"
echo "OC_ADMIN set to $OC_ADMIN"
echo "OC_USER set to $OC_USER"
echo "OC_SQUONK_HOST set to $OC_SQUONK_HOST"
echo "OC_NFS_SERVER set to $OC_NFS_SERVER"
echo "KEYCLOAK_SERVER_URL set to $KEYCLOAK_SERVER_URL"
echo "KEYCLOAK_REALM set to $KEYCLOAK_REALM"
echo "Using OpenShift version $OC_OPENSHIFT_VERSION"

