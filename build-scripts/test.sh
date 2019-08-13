#!/bin/bash

# A build script used by Travis

# Do ChemAxon files exist? Some tests rely on these files.
# If not present we'll set a corresponding environment variable.
if [[ ! -f "$TRAVIS_BUILD_DIR"/data/licenses/license.cxl ]]; then
  export CHEMAXON_LICENCE_ABSENT=yes
fi
if [[ ! -f "$TRAVIS_BUILD_DIR"/docker/deploy/images/chemservices/chemaxon_reaction_library.zip ]]; then
  export CHEMAXON_LIBRARY_ABSENT=yes
fi

cd "$TRAVIS_BUILD_DIR"/components || exit
./gradlew test
