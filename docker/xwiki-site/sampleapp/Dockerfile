FROM tomcat:7-jre8
MAINTAINER Tim Dudgeon <tdudgeon@informaticsmatters.com>

# delete the standard tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*
    
COPY server.xml /usr/local/tomcat/conf/
COPY keystore.jks /usr/local/tomcat/keystore.jks
COPY server.crt /usr/local/tomcat/server.crt
RUN keytool -import -v -trustcacerts -alias keycloakserver -file /usr/local/tomcat/server.crt -keystore /etc/ssl/certs/java/cacerts -storepass changeit -noprompt
COPY keycloak-jars/* /usr/local/tomcat/lib/
RUN mkdir /usr/local/tomcat/webapps/sampleapp
COPY sampleapp /usr/local/tomcat/webapps/sampleapp
RUN echo JAVA_OPTS='"-Dcom.squonk.keycloak.baseurl=$KEYCLOAK_SERVER_URL"' > /usr/local/tomcat/bin/setenv.sh