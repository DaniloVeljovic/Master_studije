#!/bin/bash

if [[ -z "$ES_DATA_DIR" ]]; then
  echo "ES_DATA_DIR environment variable is not set"
  echo "Set it and make sure it points to dataset folder"
  exit 1
fi

docker stop es-ml-streaming-app es-kafka-ml-producer-app kafka-server zookeeper-server spark-master spark-worker-1 spark-worker-2
docker rm es-ml-streaming-app es-kafka-ml-producer-app kafka-server zookeeper-server spark-master spark-worker-1 spark-worker-2

docker-compose -f docker-compose-predictions.yml down
docker-compose -f docker-compose-predictions.yml up --build