FROM openjdk:8-jre
LABEL maintainer="achristie@informaticsmatters.com"
RUN wget -q https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/3.2.1/flyway-commandline-3.2.1.zip && \
    unzip flyway-commandline-3.2.1.zip && \
    rm flyway-commandline-3.2.1.zip && \
    cd flyway-3.2.1/drivers/ && \
    rm -f * && \
    wget -q https://repo1.maven.org/maven2/org/postgresql/postgresql/9.4-1201-jdbc41/postgresql-9.4-1201-jdbc41.jar && \
    rm ../conf/flyway.conf

RUN useradd -u 1001 -U -G 0 -m flyway
ENV HOME /home/flyway

# Install S2I Scripts ---------------------------------------------------------

ARG s2i_dir=s2i

# Path to the image's custom S2I scripts (the container home directory).
# The HOME directory in the base image is '/home/jenkins'.
LABEL io.openshift.s2i.scripts-url="image://${HOME}/${s2i_dir}"

WORKDIR ${HOME}/${s2i_dir}
COPY s2i-scripts/* ${HOME}/${s2i_dir}/

RUN chown -R flyway:flyway ${HOME} && \
    chown -R flyway:flyway /flyway-3.2.1 && \
    chmod -R a+rw /flyway-3.2.1

# Finally ---------------------------------------------------------------------

WORKDIR /flyway-3.2.1
USER 1001

# Clear any exisiting (base image)
# entrypoint and command...
ENTRYPOINT []
CMD []
