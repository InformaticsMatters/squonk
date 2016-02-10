#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash clean.sh

rabbitmqctl delete_vhost /squonk
echo deleted /squonk virtualhost
rabbitmqctl delete_user squonk
echo deleted squonk user


rabbitmqctl add_user squonk ${RABBITMQ_SQUONK_PASS:-squonk}
echo created squonk user with password ${RABBITMQ_SQUONK_PASS:-squonk}

rabbitmqctl add_vhost /squonk
rabbitmqctl set_permissions -p /squonk admin  ".*" ".*" ".*"
rabbitmqctl set_permissions -p /squonk squonk ".*" ".*" ".*"
echo created /squonk virtualhost
