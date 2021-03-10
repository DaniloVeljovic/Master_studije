# BigData
Monorepo containing projects for BigData Course for Master studies at Faculty of Electronic Engineering

Focus is on BigData technologies like [Apache Spark](https://spark.apache.org/), [Hadoop](https://hadoop.apache.org/), [Kafka](https://kafka.apache.org/).

All projects and dependencies are container based.

Implemented in [Scala](https://www.scala-lang.org/).

# Requirements

- docker - https://www.docker.com/
- docker-compose - https://docs.docker.com/compose/install/
- dataset - https://drive.google.com/file/d/1Pg-jr6AhN4j3DS86lQD-mCKwAKfKyTum/view?usp=sharing

# Dataset
Home page: [http://extrasensory.ucsd.edu/](http://extrasensory.ucsd.edu/)

A dataset for behavioral context recognition in-the-wild from mobile sensors
- Large scale: over 300k examples (minutes) from 60 users.
- Everyday devices: sensors from smartphone (iPhone/Android) and smartwatch.
- Diverse sensors: heterogeneous measurements from different sensors.
- In-the-Wild: data was collected from users that were engaged in their regular natural behavior.
- Rich context: annotations are combinations of context labels from a large vocabulary.
- Publicly available: everyone is invited to download the dataset for free and use it.

The ExtraSensory dataset contains data from 60 users (also referred to as subjects or participants), each identified with a universally unique identifier (UUID).
From every user it has thousands of examples, typically taken in intervals of 1 minute (but not necessarily in one long sequence, there are time gaps).
Every example contains measurements from sensors (from the user's personal smartphone and from a smartwatch that we provided).
Most examples also have context labels self-reported by the user.


Sensor groups

| sensor              | details                                                                                      | dimension   | #us | #ex     |
|---------------------|----------------------------------------------------------------------------------------------|-------------|-----|---------|
| accelerometer       | Tri-axial direction and magnitude of acceleration. 40Hz for ~20sec.                          | (~800) x 3  | 60  | 308,306 |
| gyroscope           | Rate of rotation around phone's 3 axes. 40Hz for ~20sec.                                     | (~800) x 3  | 57  | 291,883 |
| magnetometer        | Tri-axial direction and magnitude of magnetic field. 40Hz for ~20sec.                        | (~800) x 3  | 58  | 282,527 |
| watch accelerometer | Tri-axial acceleration from the watch. 25Hz for ~20sec.                                      | (~500) x 3  | 56  | 210,716 |
| watch compass       | Watch heading (degrees). nC samples (whenever changes in 1deg).                              | nC x 1      | 53  | 126,781 |
| location            | Latitude, longitude, altitude, speed, accuracies. nL samples (whenever changed enough).      | nL x 6      | 58  | 273,737 |
| location (quick)    | Quick location-variability features (no absolute coordinates) calculated on the phone.       | 1 x 6       | 58  | 263,899 |
| audio               | 22kHz for ~20sec. Then 13 MFCC features from half overlapping 96msec frames.                 | (~430) x 13 | 60  | 302,177 |
| audio magnitude     | Max absolute value of recorded audio, before it was normalized.                              | 1           | 60  | 308,877 |
| phone state         | App status, battery state, WiFi availability, on the phone, time-of-day.                     | 5 discrete  | 60  | 308,320 |
| additional          | Light, air pressure, humidity, temperature, proximity. If available sampled once in session. | 5           | --- | ---     |

Some additional labels reported by users

| Label          | #users | #examples |
|----------------|--------|-----------|
| OR_indoors     | 59     | 184692    |
| LOC_home       | 57     | 152892    |
| SITTING        | 60     | 136356    |
| PHONE_ON_TABLE | 53     | 115037    |
| LYING_DOWN     | 58     | 104210    |
| SLEEPING       | 53     | 83055     |
| AT_SCHOOL      | 49     | 42331     |
| ...            | ...    | ...       |


Full dataset consists of multiple parts:
- Original context labels
- Mood labels
- Absolute locations
- Raw sensor measurements

For these projects, dataset is pre-formatted (link in requirements section points to it).
CSV files were composed by joining raw sensor measurements and absolute locations parts of extrasensory datasets.
Basically rows from original csv file have appended latitude and longitude columns at the end, that's the only formatting done.


Labels of interest:
- timestamp - seconds since epoch
- latitude - absolute component of location
- longitude - absolute component of location

# Running

First make sure that `ES_DATA_DIR` environment variable is set on host machine - it should point to the folder where [dataset](https://drive.google.com/file/d/1Pg-jr6AhN4j3DS86lQD-mCKwAKfKyTum/view?usp=sharing) is downloaded and extracted (folder should contain a list of .csv files)

Navigate to project and execute `./start.sh`

# Projects

## Project1

First project emphasizes spark and hadoop workflow.

Nodes in the system:
- application
- spark master
- spark worker(s)
- hadoop

Application takes spark master server url and data source (path to the hadoop file system) from environment variables:
```
ES_SPARK_MASTER=spark://spark-master:7077
ES_DATA_SOURCE=hdfs://hadoop:9000/data
```
Application container is configured in a way that it waits for a hadoop to be ready before it continues execution.
Environment variable:
 ```
ES_HEALTHCHECK_ENDPOINT=http://hadoop:50070/webhdfs/v1/data?op=GETFILESTATUS
```
is url to hdfs rest api which will be available when hadoop is ready. 
After data is read from hdfs, application is submitted to spark master which distributes jobs among workers.

Tasks (based on sensor-captured and user-reported data):
- Print time in minutes that users spent with friends relevant to location and time range
- Print time in minutes that users spent with phone in their hand relevant to location and time range
- Print minimum, maximum and average value of normalization multiplier relevant to location and time range


## Project2

The second project is about processing events from kafka topic via spark streaming. 

Nodes in the system:
- kafka producer
- spark consumer
- spark master
- spark worker(s)
- hadoop
- mongodb

Kafka producer reads from hdfs and sends row by row (source is a csv file) on topic.
Spark consumer is creating a streaming context related to kafka producer and listens on given topic.
Received events are processed and results (for a time window duration) are persisted in mongodb.

Tasks (based on sensor-captured and user-reported data):
- Store minimum, maximum and average value for watch acceleration properties
- Store minimum, maximum and average value for maximum audio properties
- Store 3 most popular locations where users were sitting


## Project3

Third project introduces machine learning and application of it on the ExtraSensory dataset.
Project consists of 3 applications and purpose of each of them will be explained.

Nodes in the system (services used by container apps):
- spark master
- spark workers
- kafka server
- zookeeper server
- hadoop (hosts dataset and trained model)

### Applications

#### Machine learning 
Loads dataset from hadoop and performs a model training using RandomForestClassificator. After it finishes, trained model will be saved on a HDFS.

#### Spark Streaming
First loads trained the model, then subscribes to kafka topic and predicts labels for received events based on the loaded model.

#### Kafka producer
Periodically sends events which will be processed by streaming application.

### Running
This one is a bit more complicated than previous projects, so it has separate docker compose configurations and shell scripts.

1. Execute `start-hadoop.sh` and let it run in the background as it will be needed for other applications.
2. Execute `start-machine-learning.sh`. Wait until `es-spark-machine-learning-app` finishes and terminate process (to release resources and containers).
3. Execute `start-predictions.sh`. Predictions will be displayed in the console.





