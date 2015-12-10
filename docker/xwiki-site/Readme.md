## Introduction
This sets up a simple test environment for using the Keycloak SSO solution with XWiki. 

IMPORTANT: this setup assumes that the docker host ip address is 192.168.59.103.
If you have a differnt ip then you will need to make changes to (at least) the keycloak.json files and the Valid Redirect URIs for the sampleapp and xwiki clients in the squonk realm of keycloak. In a production setup permanent DNS names should be in use and avoid this problem.

## Building
### 1 Setup
First you MUST set an environment variable that points to the keycloak server (this will depend on the ip address of the docker host).

export KEYCLOAK_SERVER_URL="http://192.168.59.103:8080"

Replace localhost with the ip or hostname of your docker host if its not 192.168.59.103
Also, if its not 192.168.59.103 tehn you will also need to change the realm configuration that's imported in step 3

### 2 Build
```sh
docker-compose build
```

### 3 Import realm configuration into Keycloak 
docker-compose up -d postgres

If your docker host is not 192.168.59.103 (and it probably not) then you need to edit the squonk-realm.json file that will be imported.
You can do this in one command like this:

``` sh
sed 's/192.168.59.103/localhost/g' squonk-realm.json > yyy.json
```

(replacing localhost with the name/ip of your docker host)

Import the realm definition with this:

```sh
docker run -it --link xwikisite_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=keycloak --rm -v $PWD:/tmp/json jboss/keycloak-postgres /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-realm.json -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```

(replace squonk-realm.json with yyy.json if you performed the edit at the start of this section)

Then Ctrl-C to terminate once started.



### 4 Fire up all containers
Now start the whole stack:
```sh  
docker-compose up -d
```

Access sampleapp at: http://192.168.59.103:9080/sampleapp/index.html
Access xwiki at: http://192.168.59.103/

## Other info

If needed export the realm definition from keycloak use this:

```sh
docker run -it --link xwikisite_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=keycloak --rm -v $PWD:/tmp/json jboss/keycloak-postgres /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-realm.json -Dkeycloak.migration.realmName=squonk
```