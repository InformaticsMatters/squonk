#!/bin/bash

# A script used by Travis
#
# If you're a user then execute from the project root,
# e.g. ./scripts/publish-latest.sh

set -eo pipefail

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# We assume we're on master (we should be) and there's no tag.
export SQUONK_IMAGE_TAG="latest"
echo "SQUONK_IMAGE_TAG is $SQUONK_IMAGE_TAG"

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the application docker images
# and push to docker.io. This will either be a tag
# or 'latest'.

pushd "$PROJECT_DIR"/components || exit

./gradlew dockerBuildImages -x test

docker push squonk/chemservices-basic:"$SQUONK_IMAGE_TAG"
docker push squonk/chemcentral-search:"$SQUONK_IMAGE_TAG"
docker push squonk/coreservices:"$SQUONK_IMAGE_TAG"
docker push squonk/cellexecutor:"$SQUONK_IMAGE_TAG"
docker push squonk/jobexecutor-keycloak:"$SQUONK_IMAGE_TAG"
docker push squonk/chemcentral-loader:"$SQUONK_IMAGE_TAG"
docker push squonk/flyway:"$SQUONK_IMAGE_TAG"
docker push squonk/flyway-2:"$SQUONK_IMAGE_TAG"

popd || exit
