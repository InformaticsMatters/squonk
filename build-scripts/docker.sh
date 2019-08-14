#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

# Construct the application docker images
# but we do not push to docker.io.

./gradlew chem-services-rdkit-search:buildDockerImage -x test
./gradlew core-services-server:buildDockerImage -x test
./gradlew cell-executor:dockerBuildImage -x test
./gradlew job-executor:buildDockerImage -x test
./gradlew rdkit-databases:dockerBuildImage -x test
./gradlew database:buildDockerImage -x test
