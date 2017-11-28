#!/bin/bash

if [ ! $OC_DEPLOYMENT == 'squonk' ]; then
    echo "ERROR: wrong deployment. Edit and run 'setenv.sh' to configure the project"
    exit 1
fi

