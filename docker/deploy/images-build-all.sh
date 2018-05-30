#!/bin/bash

set -e

./images-build-core.sh
./images-build-services.sh
./images-build-portal.sh

