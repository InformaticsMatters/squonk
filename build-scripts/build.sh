#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

# Build the application targets
#
# squonk/chemcentral-search
# squonk/coreservices
# squonk/cellexecutor
# squonk/jobexecutor
# squonk/chemcentral-loader
# squonk/flyway

./gradlew chem-services-rdkit-search:build
./gradlew core-services-server:build
./gradlew cell-executor:build
./gradlew job-executor:build
./gradlew rdkit-databases:build
./gradlew database:build
