#!/bin/sh

#Commented lines are for postgresql configuration and reserved for future support.

#if test -z "$POSTGRES_PORT" -o -z "$POSTGRES_ENV_POSTGRES_USER"; then
#  echo '$POSTGRES_PORT and $POSTGRES_ENV_POSTGRES_USER not defined. Did you forget to docker --link?'
#  exit 1
#fi

export JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true"

#postgres_connect_string="$(echo "$POSTGRES_PORT" | sed -r 's/^tcp:\/\/(.+)/jdbc:postgresql:\/\/\1\/'"$POSTGRES_ENV_POSTGRES_USER"'/' )"
#echo "Connecting to postgres via '$postgres_connect_string'"
#export CATALINA_OPTS="-Xmx1024m -XX:MaxPermSize=192m -Dpostgres_connect_string=$postgres_connect_string"
# Then include the directive $postgresql_connect_string in any xml config file.

export CATALINA_OPTS='-Xmx1024m -XX:MaxPermSize=192m'
