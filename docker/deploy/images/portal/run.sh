#!/usr/bin/env bash

# TODO - this file should no longer be needed

echo "copying files"
cp /tomcat-goodies/context.xml /usr/local/tomcat/webapps/portal/META-INF/context.xml
cp /tomcat-goodies/keycloak.json /usr/local/tomcat/webapps/portal/WEB-INF/keycloak.json
echo "starting tomcat"
exec catalina.sh run