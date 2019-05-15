FROM informaticsmatters/tomcat:8.0-jre8
LABEL maintainer="achristie@informaticsmatters.com"
RUN rm -rf /usr/local/tomcat/webapps/*

# Install S2I Scripts ---------------------------------------------------------

ARG s2i_dir=s2i

# Path to the image's custom S2I scripts (the container home directory).
# The HOME directory in the base image is '/home/jenkins'.
LABEL io.openshift.s2i.scripts-url="image:///home/${s2i_dir}"

USER 0
WORKDIR /home/${s2i_dir}
COPY s2i-scripts/* /home/${s2i_dir}/
RUN chown -R 501:501 /home

# Finally ---------------------------------------------------------------------

WORKDIR /usr/local/tomcat
USER 501

# Clear any exisiting (base image)
# entrypoint and command...
ENTRYPOINT []
CMD []
