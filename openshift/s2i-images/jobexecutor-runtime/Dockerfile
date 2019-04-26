FROM informaticsmatters/tomcat-keycloak:8.5-jre8-3.4.3
LABEL maintainer="achristie@informaticsmatters.com"

# Install S2I Scripts ---------------------------------------------------------

ARG s2i_dir=s2i

# Path to the image's custom S2I scripts (the container home directory).
# The HOME directory in the base image is '/home/jenkins'.
LABEL io.openshift.s2i.scripts-url="image:///home/tomcat/${s2i_dir}"

USER root
WORKDIR /home/tomcat/${s2i_dir}
COPY s2i-scripts/* /home/tomcat/${s2i_dir}/
RUN chown -R tomcat:tomcat /home/tomcat/${s2i_dir}

# Back to image's original user (tomcat is 501 in this image) and path...
USER 501
WORKDIR /usr/local/tomcat
