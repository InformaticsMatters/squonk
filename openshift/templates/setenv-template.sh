#@IgnoreInspection BashAddShebang

# You must define these...
export OC_MASTER_HOSTNAME=example.org
export KEYCLOAK_SECRET=secret-from-squonk-client-in-realm

# You probably should change these:
export OC_CERTS_PASSWORD=changeme

# You might want to adjust these...
export OC_DEPLOYMENT=squonk
export OC_ADMIN=admin
export OC_USER=developer
export OC_MASTER_URL=https://${OC_MASTER_HOSTNAME}:8443
export OC_ROUTES_BASENAME=${OC_MASTER_HOSTNAME}.nip.io
export KEYCLOAK_SERVER_URL=https://sso.${OC_ROUTES_BASENAME}/auth
export KEYCLOAK_REALM=squonk
export OC_INFRA_PROJECT=squonk-infra
export OC_PROJECT=squonk
export OC_SQUONK_APP=squonk-notebook
export OC_SQUONK_HOST=${OC_SQUONK_APP}.${OC_ROUTES_BASENAME}


echo "OC_PROJECT set to $OC_PROJECT"
echo "OC_INFRA_PROJECT set to $OC_INFRA_PROJECT"
echo "OC_MASTER_HOSTNAME set to $OC_MASTER_HOSTNAME"
echo "OC_ROUTES_BASENAME set to $OC_ROUTES_BASENAME"
echo "OC_ADMIN set to $OC_ADMIN"
echo "OC_USER set to $OC_USER"
echo "OC_SQUONK_HOST set to $OC_SQUONK_HOST"
echo "KEYCLOAK_SERVER_URL set to $KEYCLOAK_SERVER_URL"
echo "KEYCLOAK_REALM set to $KEYCLOAK_REALM"
echo "KEYCLOAK_SECRET set to $KEYCLOAK_SECRET"

