#!/bin/sh

export JAVA_OPTS="${JAVA_OPTS}\
 -Djava.awt.headless=true\
 -Dorg.squonk.keycloak.baseurl=$KEYCLOAK_SERVER_URL\
 -Dorg.squonk.postgres.xwiki.password=$POSTGRES_XWIKI_PASSWORD\
 -Dorg.squonk.public_host=$PUBLIC_HOST"

export CATALINA_OPTS='-Xmx1024m -XX:MaxPermSize=192m'
