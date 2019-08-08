#!/bin/bash

cd "$TRAVIS_BUILD_DIR"/components || exit

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the CellExecutor image
# and push (as latest)
./gradlew cell-executor:dockerBuildImage -x test
docker images
docker push squonk/cellexecutor:latest