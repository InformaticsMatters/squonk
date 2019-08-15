#!/bin/bash

# A script used by users and Travis.
#
# If you're a user then execute from the project root,
# e.g. ./scripts/compile.sh

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

pushd "$PROJECT_DIR"/components || exit
./gradlew assemble
popd || exit
