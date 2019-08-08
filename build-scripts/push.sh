#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

# We assume we're on master (as this is a push script)
# As we're in the Travis environment we cannot rely on getting
# the branch name using git. This works on your desktop but not in CI.
# If we do nothing our automatic tag generator will result in 'HEAD'.
# So, to avoid this, we utilise our SQUONK_IMAGE_TAG variable -
# if this is not a tagged build then set it to 'latest'
# otherwise copy the tag value.
export SQUONK_IMAGE_TAG="${TRAVIS_TAG:-latest}"

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the CellExecutor image and push...
./gradlew cell-executor:dockerBuildImage -x test
docker images
docker push squonk/cellexecutor:"${SQUONK_IMAGE_TAG}"
