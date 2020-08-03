#!/bin/bash

# A script used by Travis
#
# If you're a user then execute from the project root,
# e.g. ./scripts/docker.sh

set -eo pipefail

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# Construct the application docker images
# but we do not push to docker.io
# and do not need to run any tests.

pushd "$PROJECT_DIR"/components || exit
./gradlew dockerBuildImages -x test
popd || exit
