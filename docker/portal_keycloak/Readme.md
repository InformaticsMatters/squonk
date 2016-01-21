sed 's/192.168.59.103/172.17.0.1/g' squonk-realm.json > yyy.json



docker run -it --link portalkeycloak_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=keycloak --rm -v $PWD:/tmp/json jboss/keycloak-postgres /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/yyy.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING

