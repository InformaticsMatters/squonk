#!/bin/bash

docker-compose stop
docker-compose rm -vf
docker-compose build
docker-compose up -d

bash wait-postgres.sh

#sleep 2

#docker exec -it docker_rabbitmq_1 bash -c "rabbitmqctl add_vhost /squonk"
#echo "  created /squonk virtualhost"
#docker exec -it docker_rabbitmq_1 bash -c "rabbitmqctl add_user squonk squonk"
#echo "  created squonk user with password squonk"

#docker exec -it docker_rabbitmq_1 bash -c "rabbitmqctl set_permissions -p /       admin  '.*' '.*' '.*'"
#echo "  set permissions on /"
#docker exec -it docker_rabbitmq_1 bash -c "rabbitmqctl set_permissions -p /squonk admin  '.*' '.*' '.*'"
#docker exec -it docker_rabbitmq_1 bash -c "rabbitmqctl set_permissions -p /squonk squonk '.*' '.*' '.*'"
#echo "  set permissions on /squonk"

