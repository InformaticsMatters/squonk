FROM tomcat:7
MAINTAINER Tim Dudgeon <tdudgeon@informaticsmatters.com> 
# based on the hellyna/xwiki dockerhub image by Hellyna NG <hellyna@hellyna.com>

RUN rm -rf webapps/* &&\
 curl -L\
 'http://download.forge.ow2.org/xwiki/xwiki-enterprise-web-7.3.war'\
 -o xwiki.war &&\
 unzip -d webapps/ROOT xwiki.war &&\
 rm -f xwiki.war

#RUN curl -L \
# 'https://jdbc.postgresql.org/download/postgresql-9.4-1202.jdbc41.jar' \
# -o 'webapps/ROOT/WEB-INF/lib/postgresql-9.4-1202.jdbc41.jar'

RUN curl -L\
 'http://central.maven.org/maven2/org/hsqldb/hsqldb/2.3.3/hsqldb-2.3.3.jar' \
 -o 'webapps/ROOT/WEB-INF/lib/hsqldb-2.3.3.jar'

COPY setenv.sh bin/
COPY catalina.policy.append catalina.policy.append

RUN cat catalina.policy.append >> conf/catalina.policy &&\
 rm catalina.policy.append &&\
 echo 'environment.permanentDirectory=/var/local/xwiki' >>\
 webapps/ROOT/WEB-INF/xwiki.properties

# copy the keycloak configs to tomcat		
COPY ./keycloak-jars/* /usr/local/tomcat/lib/
COPY ./context.xml /usr/local/tomcat/webapps/ROOT/META-INF/
COPY ./web.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/
COPY ./keycloak.json /usr/local/tomcat/webapps/ROOT/WEB-INF/
COPY ./xwiki.cfg /usr/local/tomcat/webapps/ROOT/WEB-INF/
#COPY ./hibernate.cfg.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/

