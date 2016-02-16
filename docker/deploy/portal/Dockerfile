FROM tomcat:8-jre8
MAINTAINER Tim Dudgeon <tdudgeon@informaticsmatters.com>
RUN echo JAVA_OPTS='"-Dcom.sun.jersey.server.impl.cdi.lookupExtensionInBeanManager=true -DstartDerbyServer=true -Dcom.squonk.keycloak.baseurl=$KEYCLOAK_SERVER_URL"' > /usr/local/tomcat/bin/setenv.sh
RUN rm -rf              /usr/local/tomcat/webapps/*
COPY ./tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY ./server.xml       /usr/local/tomcat/conf/server.xml
COPY ./keycloak.json    /usr/local/tomcat/webapps/ROOT/WEB-INF/
COPY ./context.xml      /usr/local/tomcat/webapps/ROOT/META-INF/

# copy the keycloak jars to the tomcat lib folder
COPY keycloak-jars-tomcat8/* /usr/local/tomcat/lib/

ADD *.war               /usr/local/tomcat/webapps/
RUN unzip -d            /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/ROOT.war &&\
 rm -f                  /usr/local/tomcat/webapps/ROOT.war

# remove unused or conflicting jars
RUN cd /usr/local/tomcat/webapps/ROOT/WEB-INF/lib && rm bcprov-jdk15-1.46.jar commons-codec-*.jar commons-logging-*.jar httpclient*.jar httpcore*.jar keycloak*.jar


COPY web.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml
COPY persistence.properties /usr/local/tomcat/webapps/ROOT/WEB-INF/persistence.properties

 
EXPOSE 8080
