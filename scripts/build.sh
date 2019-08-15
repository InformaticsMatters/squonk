#!/bin/bash

# A build script used by users and Travis.
#
# If you're a user then execute from the project root,
# e.g. ./build-scripts/build.sh

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# Do ChemAxon files exist? Some tests rely on these files.
# If not present we'll set a corresponding environment variable.
if [[ -f "$PROJECT_DIR"/data/licenses/license.cxl ]]; then
  echo "ChemAxon license file is present"
else
  echo "CAUTION: ChemAxon license file is missing"
  export CHEMAXON_LICENCE_ABSENT=yes
fi
if [[ -f "$PROJECT_DIR"/docker/deploy/images/chemservices/chemaxon_reaction_library.zip ]]; then
  echo "ChemAxon reaction library is present"
else
  echo "CAUTION: ChemAxon reaction library is missing"
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
