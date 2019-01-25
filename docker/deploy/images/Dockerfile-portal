FROM informaticsmatters/tomcat:8.0-jre8
LABEL maintainer="Tim Dudgeon<tdudgeon@informaticsmatters.com>"

USER root

RUN apt-get update -y &&\
 apt-get upgrade -y &&\
 apt-get install -y --no-install-recommends unzip &&\
 apt-get clean

# copy the keycloak jars to the tomcat lib folder
COPY keycloak-jars-tomcat8/* /usr/local/tomcat/lib/

ADD portal/*.war /usr/local/tomcat/webapps/

RUN unzip -q -d /usr/local/tomcat/webapps/portal /usr/local/tomcat/webapps/ROOT.war &&\
 rm -f /usr/local/tomcat/webapps/ROOT.war &&\
 echo JAVA_OPTS='"-Dcom.sun.jersey.server.impl.cdi.lookupExtensionInBeanManager=true -Dcom.squonk.keycloak.baseurl=$KEYCLOAK_SERVER_URL"' > /usr/local/tomcat/bin/setenv.sh &&\
 chown -R 501:0 /usr/local/tomcat/webapps/portal /usr/local/tomcat/bin/setenv.sh

# remove unused or conflicting jars
RUN cd /usr/local/tomcat/webapps/portal/WEB-INF/lib && rm commons-codec-*.jar commons-logging-*.jar httpclient-4*.jar httpcore-4*.jar\
 keycloak*.jar jackson-annotations-*.jar jackson-core-*.jar jackson-databind-*.jar servlet-api-3.0.jar

COPY portal/tomcat-users.xml /usr/local/tomcat/conf/
COPY portal/web.xml portal/beans.xml portal/persistence.properties /usr/local/tomcat/webapps/portal/WEB-INF/

USER 501
