#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash init.sh

rabbitmqctl delete_vhost /unittest
rabbitmqctl delete_vhost /prod
rabbitmqctl delete_user admin
rabbitmqctl delete_user tester

rabbitmqctl add_vhost /unittest
rabbitmqctl add_vhost /prod
rabbitmqctl add_user admin lacrocks
rabbitmqctl set_user_tags admin administrator
rabbitmqctl add_user tester lacrocks
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
rabbitmqctl set_permissions -p /unittest admin ".*" ".*" ".*"
rabbitmqctl set_permissions -p /prod admin ".*" ".*" ".*"
rabbitmqctl set_permissions -p /unittest tester ".*" ".*" ".*"
rabbitmqctl set_permissions -p /prod tester ".*" ".*" ".*"
