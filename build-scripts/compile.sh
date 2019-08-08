#!/bin/bash

cd "$TRAVIS_BUILD_DIR"/components || exit

./gradlew assemble --no-daemon
