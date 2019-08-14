FROM tomcat:8-jre8
LABEL maintainer="Tim Dudgeon <tdudgeon@informaticsmatters.com>"
RUN echo JAVA_OPTS='"-Dcom.sun.jersey.server.impl.cdi.lookupExtensionInBeanManager=true -DstartDerbyServer=true"' > /usr/local/tomcat/bin/setenv.sh
RUN rm -rf /usr/local/tomcat/webapps/*
COPY tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY server.xml /usr/local/tomcat/conf/server.xml

ADD *.war /usr/local/tomcat/webapps/
RUN unzip -d /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/ROOT.war &&\
 rm -f /usr/local/tomcat/webapps/ROOT.war
COPY web.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml

 
EXPOSE 8080
