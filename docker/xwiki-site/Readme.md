# Introduction
This sets up a simple test environment for using the Keycloak SSO solution with XWiki. 

IMPORTANT: this setup assumes that the docker host ip address is 192.168.59.103.
If you have a differnt ip then you will need to make changes to (at least) the keycloak.json files and the Valid Redirect URIs for the sampleapp and xwiki clients in the squonk realm of keycloak. In a production setup permanent DNS names should be in use and avoid this problem.

# Building

```sh
docker-compose build
```

docker-compose up -d postgres

Import the realm definition with this:

```sh
docker run -it --link xwikisite_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=keycloak --rm -v $PWD:/tmp/json jboss/keycloak-postgres /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-realm.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```

Then Ctrl-C to terminate once started.
Then start the whole stack:

```sh  
docker-compose up -d --no-recreate
```

Access sampleapp at: http://192.168.59.103:9080/sampleapp/index.html
Access xwiki at: http://192.168.59.103/

# Other info

If needed export the realm definition from keycloak use this:

```sh
docker run -it --link xwikisite_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=keycloak --rm -v $PWD:/tmp/json jboss/keycloak-postgres /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-realm.json -Dkeycloak.migration.realmName=squonk
```