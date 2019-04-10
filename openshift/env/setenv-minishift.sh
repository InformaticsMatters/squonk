#!/bin/bash

# -----------------------------------------------------------------------------
# Essential Variable Declarations
# -----------------------------------------------------------------------------

export IM_PARAMETER_FILE=params-minishift.yaml

export IM_MASTER_HOSTNAME=$(minishift ip)
