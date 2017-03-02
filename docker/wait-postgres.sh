#!/bin/bash

echo "need to wait for postgres ..."
# we need to wait for postgres to start as the next step is to populate the database
attempt=0
until nc -z -w 1 localhost 5432
do
    if [ $attempt -gt 10 ]; then 
        echo "Giving up on postgres"
	exit 1
    fi
    echo "Waiting for postgres container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done
echo "postgres up"


