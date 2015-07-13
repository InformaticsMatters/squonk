#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash init.sh

rabbitmqctl delete_vhost /unittest
rabbitmqctl delete_user tester

rabbitmqctl add_vhost /unittest
rabbitmqctl add_user tester lacrocks
rabbitmqctl set_permissions -p /unittest tester ".*" ".*" ".*"
