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
export SQUONK_IMAGE_TAG="${TRAVIS_TAG:-travis}"

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the application docker images
# and push to docker.io. This wil either be a tag
# or 'latest'

./gradlew chem-services-rdkit-search:buildDockerImage -x test
docker push squonk/chemcentral-search:"${SQUONK_IMAGE_TAG}"

./gradlew core-services-server:buildDockerImage -x test
docker push squonk/coreservices:"${SQUONK_IMAGE_TAG}"

./gradlew cell-executor:dockerBuildImage -x test
docker push squonk/cellexecutor:"${SQUONK_IMAGE_TAG}"

./gradlew job-executor:buildDockerImage -x test
docker push squonk/jobexecutor-keycloak:"${SQUONK_IMAGE_TAG}"

./gradlew rdkit-databases:dockerBuildImage -x test
docker push squonk/chemcentral-loader:"${SQUONK_IMAGE_TAG}"

./gradlew database:buildDockerImage -x test
docker push squonk/flyway:"${SQUONK_IMAGE_TAG}"
