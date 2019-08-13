#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

# Do ChemAxon files exist? Some tests rely on these files.
# If noit presetn we'll set a corresponding environment variable.
# The files are: -
# - license.cxl (expected in data/licenses)
# - chemaxon_reaction_library.zip (expected in docker/deploy/images/chemservices)
if [[ ! -f ../data/licenses/license.cxl ]]
then
  export CHEMAXON_LICENCE_ABSENT=yes
fi
if [[ ! -f ../docker/deploy/images/chemservices/chemaxon_reaction_library.zip ]]
then
  export CHEMAXON_LIBRARY_ABSENT=yes
fi

# Begin testing...

./gradlew test
