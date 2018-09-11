#!/bin/bash

# For an openshift deployment the OC_MASTER_HOSTNAME must be a public domain
# that will resolve to the master. For minishift this will be the
# IP address of the minishift VM. e.g. `minishift ip`  with `.nip.io` appended.
# e.g. 192.168.42.78.nip.io
export OC_MASTER_HOSTNAME=example.org
# The OC_MASTER_URL is used to login to the cluster.
# For an openshift deployment the OC_MASTER_URL must be a public URL
# that will resolve to the master. For minishift this should be blank.
export OC_MASTER_URL=https://example.org:8443

export OC_DEPLOYMENT=squonk
export OC_ADMIN=admin
export OC_ADMIN_PASS=admin
export OC_USER=developer

# For openshift change this to the hostname of the infra node hosting the router
export OC_ROUTES_BASENAME=${OC_MASTER_HOSTNAME}
export KEYCLOAK_SERVER_URL=https://sso.${OC_ROUTES_BASENAME}/auth
export KEYCLOAK_REALM=squonk
export KEYCLOAK_LOGOUT_REDIRECT_TO=http://home.${OC_ROUTES_BASENAME}/
export OC_INFRA_PROJECT=squonk-infra
export OC_POSTGRESQL_SERVICE=db-postgresql
export OC_PROJECT=squonk
export OC_SQUONK_APP=squonk-notebook
export OC_SQUONK_HOST=${OC_SQUONK_APP}.${OC_ROUTES_BASENAME}
export OC_SQUONK_CC_PVC_SIZE=20Gi
export OC_SQUONK_IMAGE_TAG=latest
export OC_SQUONK_SD_POSTER_IMAGE_TAG=latest
export OC_DOMAIN_NAME=novalocal
export OC_NFS_SERVER=squonk-infra.$OC_DOMAIN_NAME
export OC_NFS_PATH=/exports
export OC_SQUONK_VOLUME_TYPE=nfs
export OC_SQUONK_VOLUME_STORAGE_CLASS=SetIfUsingDynamicVolumeType
export OC_INFRA_VOLUME_TYPE=nfs
export OC_INFRA_VOLUME_STORAGE_CLASS=SetIfUsingDynamicVolumeType
# The path, relative to the directory holding this file,
# of the working copy of the Squonk pipelines repository.
# If you don't have a working copy this should be blank.
export SQUONK_PIPELINES_PATH=../../../pipelines

# A built-in user (blank to avoid)
export SQUONK_GUEST_USER=guest
export SQUONK_GUEST_PASSWORD=guest1234

# Don't really like the NoCows stuff in ansible...
export ANSIBLE_NOCOWS=1

echo "OC_PROJECT set to '$OC_PROJECT'"
echo "OC_INFRA_PROJECT set to '$OC_INFRA_PROJECT'"
echo "OC_MASTER_HOSTNAME set to '$OC_MASTER_HOSTNAME'"
echo "OC_ROUTES_BASENAME set to '$OC_ROUTES_BASENAME'"
echo "OC_MASTER_URL set to '$OC_MASTER_URL'"
echo "OC_ADMIN set to '$OC_ADMIN'"
echo "OC_USER set to '$OC_USER'"
echo "OC_SQUONK_HOST set to '$OC_SQUONK_HOST'"
echo "OC_SQUONK_IMAGE_TAG set to '$OC_SQUONK_IMAGE_TAG'"
echo "OC_SQUONK_SD_POSTER_IMAGE_TAG set to '$OC_SQUONK_SD_POSTER_IMAGE_TAG'"
echo "OC_SQUONK_VOLUME_TYPE set to $OC_SQUONK_VOLUME_TYPE"
echo "OC_SQUONK_VOLUME_STORAGE_CLASS set to $OC_SQUONK_VOLUME_STORAGE_CLASS"
echo "OC_INFRA_VOLUME_TYPE set to $OC_INFRA_VOLUME_TYPE"
echo "OC_INFRA_VOLUME_STORAGE_CLASS set to $OC_INFRA_VOLUME_STORAGE_CLASS"
echo "OC_NFS_SERVER set to '$OC_NFS_SERVER'"
echo "KEYCLOAK_SERVER_URL set to '$KEYCLOAK_SERVER_URL'"
echo "KEYCLOAK_REALM set to '$KEYCLOAK_REALM'"
echo "SQUONK_PIPELINES_PATH set to '$SQUONK_PIPELINES_PATH'"
