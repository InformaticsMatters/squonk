#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

# Build the CellExecutor image
./gradlew cell-executor:build
