FROM informaticsmatters/openjdk_docker:latest
LABEL maintainer="achristie@informaticsmatters.com"

# Install S2I Scripts ---------------------------------------------------------

ARG s2i_dir=s2i

# Path to the image's custom S2I scripts (the container home directory).
# The HOME directory in the base image is '/home/jenkins'.
LABEL io.openshift.s2i.scripts-url="image://${HOME}/${s2i_dir}"

WORKDIR ${HOME}/${s2i_dir}
COPY s2i-scripts/* ${HOME}/${s2i_dir}/
RUN chown -R 1001:1001 ${HOME}

# Finally ---------------------------------------------------------------------

WORKDIR /
USER 1001

# Clear any exisiting (base image)
# entrypoint and command...
ENTRYPOINT []
CMD []
