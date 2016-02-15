#!/bin/bash
#

if [ "$#" -eq 0 ]; then
    echo "ERROR: must specify container name(s)"
    exit
fi

for c in "$@"
do
    
echo "processing $c ..."

docker exec -it $c bash -c "rabbitmqctl delete_vhost /squonk"
docker exec -it $c bash -c "rabbitmqctl delete_user guest"
docker exec -it $c bash -c "rabbitmqctl delete_user squonk"

docker exec -it $c bash -c "rabbitmqctl add_vhost /squonk"
echo "  created /squonk virtualhost"
docker exec -it $c bash -c "rabbitmqctl add_user squonk ${RABBITMQ_SQUONK_PASS:-squonk}"
echo "  created squonk user with password ${RABBITMQ_SQUONK_PASS:-squonk}"
docker exec -it $c bash -c "rabbitmqctl change_password admin ${RABBITMQ_DEFAULT_PASS:-squonk}"
echo "  admin password set to ${RABBITMQ_DEFAULT_PASS:-squonk}"

docker exec -it $c bash -c "rabbitmqctl set_permissions -p /       admin  '.*' '.*' '.*'"
docker exec -it $c bash -c "rabbitmqctl set_permissions -p /squonk admin  '.*' '.*' '.*'"
docker exec -it $c bash -c "rabbitmqctl set_permissions -p /squonk squonk '.*' '.*' '.*'"
echo set permissions

done
