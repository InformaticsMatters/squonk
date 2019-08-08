#!/bin/bash

# A build script used by Travis
cd "$TRAVIS_BUILD_DIR"/components || exit

./gradlew assemble --no-daemon
