#!/bin/sh
#
# sets up the containers ready for use.
# once comple run them with run-containers.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

base=$PWD

echo "checking we have some content for the websites"
if [ ! -d images/nginx/sites/informaticsmatters.com/html ]; then
	echo "creating dummy content for informaticsmatters.com"
	mkdir -p images/nginx/sites/informaticsmatters.com/html || exit 1
fi
if [ ! -d images/nginx/sites/squonk.it/html ]; then
	echo "creating dummy content for squonk.it"
	mkdir -p images/nginx/sites/squonk.it/html || exit 1
	cp images/nginx/sites/index.html images/nginx/sites/squonk.it/html/ || exit 1
fi

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"


# setup nginx
sed "s/__public_host__/${PUBLIC_HOST}/g" images/nginx/default.ssl.conf.template > images/nginx/default.ssl.conf


echo "preparing postgres docker image ..."
docker-compose stop && docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres rabbitmq stage1 || exit 1

# now we can start keycloak (needs postgres to be setup before it starts)
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d keycloak stage2 || exit 1


echo "preparing rabbitmq docker image ..."
./images/rabbitmq/rabbitmq-setup.sh deploy_rabbitmq_1 || exit 1
echo "... rabbitmq container configured"
docker-compose stop rabbitmq

keycloak_url="http://${PRIVATE_HOST}:8080/auth"
echo "keycloak_url: $keycloak_url"

token=$(curl -s -k -X POST "${keycloak_url}/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded"\
 -d "username=admin" -d "password=${KEYCLOAK_PASSWORD:-squonk}" -d "grant_type=password" -d "client_id=admin-cli" \
 | jq -r '.access_token') || exit 1
echo "token: $token"

# substitute the realm json file need by keycloak
sed "s/__public_host__/${PUBLIC_HOST}/g" images/squonk-realm.json.template > images/squonk-realm.json
# and now create that realm in keycloak 
curl -s -k -X POST -T images/squonk-realm.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"  || exit 1
echo "squonk realm added to keycloak"

docker-compose stop
echo finished

