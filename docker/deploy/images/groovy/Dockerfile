FROM webratio/java:8
MAINTAINER Tim Dudgeon <tdudgeon@informaticsmatters.com>
# Based on the webratio/groovy Docker image, but updated to handle the switch from gvm to sdkman

# Defines environment variables
ENV HOME /root
ENV DEBIAN_FRONTEND noninteractive
ENV GROOVY_VERSION 2.4.7

# Installs curl and GVM
RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl -s "https://get.sdkman.io" | bash && \
    apt-get autoremove -y && \
    apt-get clean
    
RUN /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install groovy ${GROOVY_VERSION}"

ENV GROOVY_HOME /root/.sdkman/candidates/groovy/current
ENV PATH $GROOVY_HOME/bin:$PATH

WORKDIR /source
ENTRYPOINT ["groovy", "-Dgrape.root=/graperoot"]
