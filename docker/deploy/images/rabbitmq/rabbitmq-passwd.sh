#!/bin/bash
#

if [ "$#" -eq 0 ]; then
    echo "ERROR: must specify container name(s)"
    exit
fi

for c in "$@"
do
    
echo "processing $c ..."

docker exec -it $c bash -c "rabbitmqctl change_password admin ${RABBITMQ_DEFAULT_PASS:-squonk}"
echo "  admin password set to ${RABBITMQ_DEFAULT_PASS:-squonk}"
docker exec -it $c bash -c "rabbitmqctl change_password squonk ${RABBITMQ_SQUONK_PASS:-squonk}"
echo "  squonk password set to ${RABBITMQ_SQUONK_PASS:-squonk}"


done
