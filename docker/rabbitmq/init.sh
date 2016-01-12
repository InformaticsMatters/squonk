#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash init.sh

rabbitmqctl delete_vhost $SQUONK_RABBITMQ_VHOST
rabbitmqctl delete_user guest
rabbitmqctl delete_user $RABBITMQ_DEFAULT_USER
rabbitmqctl delete_user $SQUONK_RABBITMQ_USER

rabbitmqctl add_vhost $SQUONK_RABBITMQ_VHOST

rabbitmqctl add_user $RABBITMQ_DEFAULT_USER $RABBITMQ_DEFAULT_PASS
rabbitmqctl add_user $SQUONK_RABBITMQ_USER $SQUONK_RABBITMQ_PASS

rabbitmqctl set_user_tags $RABBITMQ_DEFAULT_USER administrator

rabbitmqctl set_permissions -p /                      $RABBITMQ_DEFAULT_USER ".*" ".*" ".*"
rabbitmqctl set_permissions -p $SQUONK_RABBITMQ_VHOST $RABBITMQ_DEFAULT_USER ".*" ".*" ".*"
rabbitmqctl set_permissions -p $SQUONK_RABBITMQ_VHOST $SQUONK_RABBITMQ_USER  ".*" ".*" ".*"
