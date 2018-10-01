#!/bin/bash
#

echo "Entering containers-setup-keycloak.sh"

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

set -e

echo starting keycloak
docker-compose stop keycloak
docker-compose rm -fv keycloak
COMPOSE_FILE="$COMPOSE_FILE:docker-compose-setup.yml"
docker-compose up -d --no-recreate keycloak stage2

keycloak_url="http://${KEYCLOAK_SERVER}:8080/auth"
echo "keycloak_url: $keycloak_url"

token=$(curl -s -k -X POST "${keycloak_url}/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded"\
 -d "username=${KEYCLOAK_USER:-admin}" -d "password=${KEYCLOAK_PASSWORD:-squonk}" -d "grant_type=password" -d "client_id=admin-cli" \
 | jq -r '.access_token')
echo "token: $token\n"

# substitute the realm json file need by keycloak
sed "s/__public_host__/${PUBLIC_HOST}/g" images/squonk-realm.json.template > images/squonk-realm.json
# and now create that realm in keycloak 
curl -s -k -X POST -T images/squonk-realm.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"  || exit 1
echo "Squonk realm added to Keycloak"

echo finished keycloak

