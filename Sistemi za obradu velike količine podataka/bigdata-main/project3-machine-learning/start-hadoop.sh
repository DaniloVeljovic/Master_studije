#!/bin/bash

if [[ -z "$ES_DATA_DIR" ]]; then
  echo "ES_DATA_DIR environment variable is not set"
  echo "Set it and make sure it points to dataset folder"
  exit 1
fi

docker network rm project3-network
docker network create project3-network

docker stop hadoop
docker rm hadoop

docker-compose -f docker-compose-hadoop.yml down
docker-compose -f docker-compose-hadoop.yml up --build