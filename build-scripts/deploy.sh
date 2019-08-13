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
LATEST_TAG='travis'
export SQUONK_IMAGE_TAG="${TRAVIS_TAG:-$LATEST_TAG}"

# Login to docker.io
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin docker.io

# Construct the application docker images
# and push to docker.io. This wil either be a tag
# or 'latest'

./gradlew chem-services-rdkit-search:buildDockerImage -x test
./gradlew core-services-server:buildDockerImage -x test
./gradlew cell-executor:dockerBuildImage -x test
./gradlew job-executor:buildDockerImage -x test
./gradlew rdkit-databases:dockerBuildImage -x test
./gradlew database:buildDockerImage -x test

docker push squonk/chemcentral-search:"$SQUONK_IMAGE_TAG"
docker push squonk/coreservices:"$SQUONK_IMAGE_TAG"
docker push squonk/cellexecutor:"$SQUONK_IMAGE_TAG"
docker push squonk/jobexecutor-keycloak:"$SQUONK_IMAGE_TAG"
docker push squonk/chemcentral-loader:"$SQUONK_IMAGE_TAG"
docker push squonk/flyway:"$SQUONK_IMAGE_TAG"

# If this is not 'latest'
# then also tag and push new latest images...

if [[ "$SQUONK_IMAGE_TAG" != "$LATEST_TAG" ]]
then
    docker tag squonk/chemcentral-search:"$SQUONK_IMAGE_TAG" squonk/chemcentral-search:"$LATEST_TAG"
    docker tag squonk/coreservices:"$SQUONK_IMAGE_TAG" squonk/coreservices:"$LATEST_TAG"
    docker tag squonk/cellexecutor:"$SQUONK_IMAGE_TAG" squonk/cellexecutor:"$LATEST_TAG"
    docker tag squonk/jobexecutor-keycloak:"$SQUONK_IMAGE_TAG" squonk/jobexecutor-keycloak:"$LATEST_TAG"
    docker tag squonk/chemcentral-loader:"$SQUONK_IMAGE_TAG" squonk/chemcentral-loader:"$LATEST_TAG"
    docker tag squonk/flyway:"$SQUONK_IMAGE_TAG" tag squonk/flyway:"$LATEST_TAG"

    docker push squonk/chemcentral-search:"$LATEST_TAG"
    docker push squonk/coreservices:"$LATEST_TAG"
    docker push squonk/cellexecutor:"$LATEST_TAG"
    docker push squonk/jobexecutor-keycloak:"$LATEST_TAG"
    docker push squonk/chemcentral-loader:"$LATEST_TAG"
    docker push squonk/flyway:"$LATEST_TAG"
fi
