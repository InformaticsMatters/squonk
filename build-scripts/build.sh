#!/bin/bash

# A build script used by users and Travis.
#
# If you're a user then execute from the project root,
# e.g. ./build-sceripts/deploy.sh

# Set the project path
if [[ ! "$TRAVIS_BUILD_DIR" ]]; then
  export PROJECT_DIR="$PWD"
else
  export PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# The CHEMAXON_HOME Variable
# (the path to the licence directory)
export CHEMAXON_HOME="$PROJECT_DIR"/data/licenses
# Do ChemAxon files exist? Some tests rely on these files.
# If not present we'll set a corresponding environment variable.
if [[ ! -f "$PROJECT_DIR"/data/licenses/license.cxl ]]; then
  export CHEMAXON_LICENCE_ABSENT=yes
fi
if [[ ! -f "$PROJECT_DIR"/docker/deploy/images/chemservices/chemaxon_reaction_library.zip ]]; then
  export CHEMAXON_LIBRARY_ABSENT=yes
fi

# Define the working directory for cell execution.
# This is used as a default when no explicit directory is provided.
# Some unit tests expect to create this directory, delete it
# and write files to it so it has to be somehere Travis will let us write.
export SQUONK_DOCKER_WORK_DIR="$PROJECT_DIR"/tmp

pushd "$PROJECT_DIR"/components
./gradlew build
popd
