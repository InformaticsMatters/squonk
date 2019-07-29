FROM openjdk:8
LABEL maintainer="achristie@informaticsmatters.com"

# Install a User --------------------------------------------------------------

# Create the all-important non root user
# and change ownership of relevant files.
ENV HOME=/home/squonk
RUN useradd -u 1001 -r -g 0 -d ${HOME} -s /sbin/nologin squonk && \
    mkdir -p ${HOME} && \
    chown -R 1001:0 ${HOME} && chmod -R g+rwX ${HOME}

# Java Env --------------------------------------------------------------------

# Make JVM respect CPU and RAM limits
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Install Gradle --------------------------------------------------------------

WORKDIR /opt/gradle
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
