#!/bin/bash

# A script used by Travis
#
# If you're a user then execute from the project root,
# e.g. ./scripts/publish.sh

set -eo pipefail

# Set the project path
if [[ -z "$TRAVIS_BUILD_DIR" ]]; then
  PROJECT_DIR="$PWD"
else
  PROJECT_DIR="$TRAVIS_BUILD_DIR"
fi

# We expect TRAVIS_TAG to be defined so assert that...
: "${TRAVIS_TAG?Need to set TRAVIS_TAG}"
export SQUONK_IMAGE_TAG="${TRAVIS_TAG}"
echo "SQUONK_IMAGE_TAG is $SQUONK_IMAGE_TAG"

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the application docker images
# and push to docker.io.
# As we're assuming we've been run because we're on master
# then this will will be a tag and 'latest'.

pushd "$PROJECT_DIR"/components || exit

./gradlew dockerBuildImages -x test

docker push squonk/chemservices-basic:"$SQUONK_IMAGE_TAG"
docker push squonk/chemcentral-search:"$SQUONK_IMAGE_TAG"
docker push squonk/coreservices:"$SQUONK_IMAGE_TAG"
docker push squonk/cellexecutor:"$SQUONK_IMAGE_TAG"
docker push squonk/jobexecutor-keycloak:"$SQUONK_IMAGE_TAG"
docker push squonk/chemcentral-loader:"$SQUONK_IMAGE_TAG"
docker push squonk/flyway:"$SQUONK_IMAGE_TAG"

# We're invoked on the `master` branch (or we should habve been)
# so also publish 'latest' (basically re-tag the image and push)...

docker tag squonk/chemservices-basic:"$SQUONK_IMAGE_TAG" squonk/chemservices-basic:latest
docker tag squonk/chemcentral-search:"$SQUONK_IMAGE_TAG" squonk/chemcentral-search:latest
docker tag squonk/coreservices:"$SQUONK_IMAGE_TAG" squonk/coreservices:latest
docker tag squonk/cellexecutor:"$SQUONK_IMAGE_TAG" squonk/cellexecutor:latest
docker tag squonk/jobexecutor-keycloak:"$SQUONK_IMAGE_TAG" squonk/jobexecutor-keycloak:latest
docker tag squonk/chemcentral-loader:"$SQUONK_IMAGE_TAG" squonk/chemcentral-loader:latest
docker tag squonk/flyway:"$SQUONK_IMAGE_TAG" squonk/flyway:latest

docker push squonk/chemservices-basic:latest
docker push squonk/chemcentral-search:latest
docker push squonk/coreservices:latest
docker push squonk/cellexecutor:latest
docker push squonk/jobexecutor-keycloak:latest
docker push squonk/chemcentral-loader:latest
docker push squonk/flyway:latest

popd || exit
