FROM centos:7
LABEL maintainer="achristie@informaticsmatters.com"

# Install a User --------------------------------------------------------------

# Create the all-important non root user
# and change ownership of relevant files.
ENV HOME=/home/squonk
RUN useradd -u 1001 -r -g 0 -d ${HOME} -s /sbin/nologin squonk && \
    mkdir -p ${HOME} && \
    chown -R 1001:0 ${HOME} && chmod -R g+rwX ${HOME}

# Install Java ----------------------------------------------------------------

# Gradle needs JAVA_HOME but be careful with JAVA_HOME.
# It might change if you build this container again.
RUN yum -y install java-1.8.0-openjdk-devel
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.201.b09-2.el7_6.x86_64/jre

# Install Gradle --------------------------------------------------------------

WORKDIR /opt/gradle
RUN yum -y update && \
    yum -y install unzip wget
RUN wget https://services.gradle.org/distributions/gradle-3.5.1-bin.zip && \
    unzip -d /opt/gradle gradle-3.5.1-bin.zip
ENV PATH=${PATH}:/opt/gradle/gradle-3.5.1/bin

# Install S2I Scripts ---------------------------------------------------------

ARG s2i_dir=s2i

# Path to the image's custom S2I scripts
# (inside the container home directory).
LABEL io.openshift.s2i.scripts-url="image://${HOME}/${s2i_dir}"

WORKDIR ${HOME}/${s2i_dir}
COPY s2i-scripts/* ${HOME}/${s2i_dir}/
RUN chown -R 1001:1001 ${HOME}

# Finally ---------------------------------------------------------------------

WORKDIR ${HOME}
USER 1001

# Clear any exisiting (base image)
# entrypoint and command...
ENTRYPOINT []
CMD []
