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
	mkdir images/nginx/sites/informaticsmatters.com/html || exit 1
fi
if [ ! -d images/nginx/sites/squonk.it/html ]; then
	echo "creating dummy content for squonk.it"
	mkdir images/nginx/sites/squonk.it/html || exit 1
	cp images/nginx/sites/index.html images/nginx/sites/squonk.it/html/ || exit 1
fi

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# substitute the realm json file need by keycloak
sed "s/__public_host__/${PUBLIC_HOST}/g" images/squonk-realm.json.template > images/squonk-realm.json
# set up the proxy details in the tomcat apps 
sed "s/__public_host__/${PUBLIC_HOST}/g" images/xwiki/server.xml.template > images/xwiki/server.xml
# setup xwiki connection to postgres
sed "s/__postgres_xwiki_password__/${POSTGRES_XWIKI_PASS}/g" images/xwiki/hibernate.cfg.xml.template > images/xwiki/hibernate.cfg.xml
# setup nginx
sed "s/__public_host__/${PUBLIC_HOST}/g" images/nginx/default.conf.template > images/nginx/default.conf

images="rabbitmq postgres keycloak xwiki"

docker-compose stop && docker-compose rm -f $images && docker-compose build $images || exit 1


echo "preparing postgres docker image ..."
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres rabbitmq stage1 || exit 1

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

curl -s -k -X POST -T images/squonk-realm.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"  || exit 1
echo "squonk realm added to keycloak"

docker-compose stop
echo finished
