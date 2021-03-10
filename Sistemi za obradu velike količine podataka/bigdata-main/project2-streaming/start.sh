#!/bin/bash

if [[ -z "$ES_DATA_DIR" ]]; then
  echo "ES_DATA_DIR environment variable is not set"
  echo "Set it and make sure it points to dataset folder"
  exit 1
fi

docker kill es-kafka-producer-app es-spark-consumer-app kafka-server zookeeper-server hadoop spark-master spark-worker-1 spark-worker-2 cassandra-db 
docker rm es-kafka-producer-app es-spark-consumer-app kafka-server zookeeper-server hadoop spark-master spark-worker-1 spark-worker-2 cassandra-db

docker-compose down
docker-compose up --build
