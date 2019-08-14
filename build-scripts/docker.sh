#!/bin/bash

# A build script used by Travis
#
# If you're a user then execute from the project root,
# e.g. ./build-scripts/docker.sh

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# Construct the application docker images
# but we do not push to docker.io
# and do not need to run any tests.

pushd "$PROJECT_DIR"/components

./gradlew chem-services-rdkit-search:buildDockerImage -x test
./gradlew core-services-server:buildDockerImage -x test
./gradlew cell-executor:dockerBuildImage -x test
./gradlew job-executor:buildDockerImage -x test
./gradlew rdkit-databases:dockerBuildImage -x test
./gradlew database:buildDockerImage -x test

popd
