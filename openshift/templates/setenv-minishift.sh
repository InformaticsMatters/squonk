#!/bin/bash

# A set of Squonk environment variables suitable for MiniShift.
# MiniShift must be running before you 'source' this as it uses
# MiniShift ip to get the IP address of the MiniShift cluster.
#
# Refer to 'setenv-template.sh' for further documentation.

export OC_MASTER_HOSTNAME=$(minishift ip):8443
export OC_MASTER_URL=

export OC_DEPLOYMENT=squonk
export OC_ADMIN=admin
export OC_ADMIN_PASSWORD=admin
export OC_USER=developer
export OC_USER_PASSWORD=developer

export OC_ROUTES_BASENAME=$(minishift ip).nip.io
export KEYCLOAK_SERVER_URL=http://sso.${OC_ROUTES_BASENAME}/auth
export KEYCLOAK_REALM=squonk
export KEYCLOAK_SSL_REQUIRED=external
export KEYCLOAK_INSECURE_ROUTE=Allow
export KEYCLOAK_LOGOUT_REDIRECT_TO=http://home.${OC_ROUTES_BASENAME}/
export OC_INFRA_PROJECT=squonk-infra
export OC_INFRA_PROJECT_DISPLAY_NAME="Squonk Infrastructure"
export OC_INFRA_HOURLY_BACKUP_COUNT=0
export OC_INFRA_HOURLY_BACKUP_SCHEDULE="7 23 * * *"
export OC_INFRA_DAILY_BACKUP_COUNT=0
export OC_INFRA_DAILY_BACKUP_SCHEDULE="37 3 * * *"
export OC_INFRA_BACKUP_VOLUME_SIZE=125Gi
export OC_INFRA_SA=squonk
export OC_POSTGRESQL_SERVICE=db-postgresql
export OC_POSTGRESQL_VOLUME_SIZE=10Gi
export OC_PROJECT=squonk
export OC_PROJECT_DISPLAY_NAME="Squonk Applications"
export OC_PROJECT_SA=squonk
export OC_SQUONK_APP=squonk-notebook
export OC_SQUONK_HOST=${OC_SQUONK_APP}.${OC_ROUTES_BASENAME}
export OC_SQUONK_CLIENT_SECRET=squonk1234
export OC_JOBEXECUTOR_HOST=jobexecutor.${OC_ROUTES_BASENAME}
export OC_SQUONK_CC_PVC_SIZE=20Gi

export OC_SQUONK_CELL_IMAGE_TAG=latest
export OC_SQUONK_CHEM_IMAGE_TAG=latest
export OC_SQUONK_CORE_IMAGE_TAG=latest
export OC_SQUONK_JE_IMAGE_TAG=latest
export OC_SQUONK_PORTAL_IMAGE_TAG=latest
export OC_SQUONK_SEARCH_IMAGE_TAG=latest
export OC_SQUONK_FLYWAY_IMAGE_TAG=latest

export OC_PIPELINES_SD_POSTER_IMAGE_TAG=latest
export OC_SQUONK_NEXTFLOW_IMAGE="informaticsmatters/nextflow:18.10.1"

export OC_DOMAIN_NAME=novalocal
export OC_NFS_SERVER=squonk-infra.$OC_DOMAIN_NAME
export OC_NFS_PATH=/exports
export OC_SQUONK_VOLUME_TYPE=minishift
export OC_SQUONK_VOLUME_STORAGE_CLASS=
export OC_INFRA_VOLUME_TYPE=minishift
export OC_INFRA_VOLUME_STORAGE_CLASS=
export OC_CHEMCENTRAL_POSTGRESQL_VOLUME_SIZE=10Gi
export OC_CHEMCENTRAL_VOLUME_TYPE=minishift
export OC_CHEMCENTRAL_VOLUME_STORAGE_CLASS=

export SQUONK_PIPELINES_PATH=../../../pipelines

export SQUONK_GUEST_USER=guest
export SQUONK_GUEST_PASSWORD=guest1234

export ANSIBLE_NOCOWS=1

echo "OC_PROJECT set to '$OC_PROJECT'"
echo "OC_INFRA_PROJECT set to '$OC_INFRA_PROJECT'"
echo "OC_MASTER_HOSTNAME set to '$OC_MASTER_HOSTNAME'"
echo "OC_ROUTES_BASENAME set to '$OC_ROUTES_BASENAME'"
echo "OC_MASTER_URL set to '$OC_MASTER_URL'"
echo "OC_ADMIN set to '$OC_ADMIN'"
echo "OC_USER set to '$OC_USER'"
echo "OC_SQUONK_HOST set to '$OC_SQUONK_HOST'"
echo "OC_JOBEXECUTOR_HOST set to '$OC_JOBEXECUTOR_HOST'"
echo "OC_SQUONK_IMAGE_TAG set to '$OC_SQUONK_IMAGE_TAG'"
echo "OC_PIPELINES_SD_POSTER_IMAGE_TAG set to '$OC_PIPELINES_SD_POSTER_IMAGE_TAG'"
echo "OC_SQUONK_VOLUME_TYPE set to $OC_SQUONK_VOLUME_TYPE"
echo "OC_SQUONK_VOLUME_STORAGE_CLASS set to $OC_SQUONK_VOLUME_STORAGE_CLASS"
echo "OC_INFRA_VOLUME_TYPE set to $OC_INFRA_VOLUME_TYPE"
echo "OC_INFRA_VOLUME_STORAGE_CLASS set to $OC_INFRA_VOLUME_STORAGE_CLASS"
echo "OC_NFS_SERVER set to '$OC_NFS_SERVER'"
echo "KEYCLOAK_SERVER_URL set to '$KEYCLOAK_SERVER_URL'"
echo "KEYCLOAK_REALM set to '$KEYCLOAK_REALM'"
echo "SQUONK_PIPELINES_PATH set to '$SQUONK_PIPELINES_PATH'"
