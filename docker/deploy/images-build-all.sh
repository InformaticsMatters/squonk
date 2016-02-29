#!/bin/bash

cd ..
./images-build-services.sh
cd deploy
./images-build-portal.sh

