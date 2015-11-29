FROM tomcat:8-jre8
MAINTAINER Tim Dudgeon <tdudgeon@informaticsmatters.com>
RUN echo JAVA_OPTS='"-Dcom.sun.jersey.server.impl.cdi.lookupExtensionInBeanManager=true -DstartDerbyServer=true"' > /usr/local/tomcat/bin/setenv.sh
RUN rm -rf /usr/local/tomcat/webapps/*
ADD *.war /usr/local/tomcat/webapps/
EXPOSE 8080
