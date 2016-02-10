#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash update-credentials.sh

rabbitmqctl change_password admin  ${RABBITMQ_ADMIN_PASS:-squonk}
rabbitmqctl change_password squonk ${RABBITMQ_SQUONK_PASS:-squonk}
