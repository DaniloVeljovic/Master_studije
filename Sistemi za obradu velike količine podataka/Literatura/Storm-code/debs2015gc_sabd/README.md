This repository contains a benchmark application for Storm. This application is called debs2015gb, because it implements the DEBS 2015 Grand Challenge ([LINK](http://dl.acm.org/citation.cfm?id=2772598)).


## Requirements ##

* Apache Storm (0.9.3 or newer versions)
* Redis


## How do I get set up? ##
 * Compile the code
 * Set up your environment (Apache, Redis)
 * Run the application 

### Compile your code ###
This project is configured as a Maven project. All you have to do to compile the code is running the following command from the root folder of the project (where the pom.xml file is located): 

```
mvn clean install package
```
Maven produces an artifact is the *target* folder, called debs2015gc-1.0.jar; this file contains the topology that you can submit to Nimbus, the centralized component of Storm is charge of running users' applications. Please, read the following section **Run the application** to further details about the configuration file needed to the topology and the command needed to launch the topology. 

### Set up your environment (Apache, Redis) ###
Install Apache Storm and Redis.

### Run the application ###
**Configuration File**
Before running, the application, you need to create a configuration file *conf.properties*. This file is contained in the repository; moreover, a default configuration file is created at the first execution of the topology, if the configuration file is missing; the default configuration file is called *conf.properties.defaults*. An example of the *conf.properties* is as follows.
```
redis.url=localhost
redis.port=6379
topology.tasks.num=32
``` 
**Submit debs2015gb to Storm**
To run the DEBS 2015 Grand Challenge application you need to run two separate application. The first one is a topology of Storm, which reads data from Redis and executes the Query 1 of DEBS 2015 GC; whereas the second one is a datasource that reads and replays data from the Grand Challenge data set by saving them on Redis. Redis represents a shared memory service that decouples the data source and the debs2015gb application.

First of all we need to submit debs2015gb to Storm. Once you compiled debs2015gb and created the configuration file, you are able to run the application. From the folder that contains both *debs2015gc-1.0.jar* and *conf.properties*, you can submit the application to Storm using the following command
```
[path apache-storm]/bin/storm jar debs2015gc-1.0.jar it.uniroma2.debs2015gc.Debs2015GrandChallenge [topologyname]
```
Now your application is running on Storm and it is ready to receive data through Redis and process them.

You can now run the second application using the following command:
```
java -cp .:debs2015gc-1.0-jar-with-dependencies.jar it.uniroma2.debs2015gc.DataSource [debs2015 dataset path] [redis ip]
```

## Contributors ##
* Matteo Nardelli

### Contribution guidelines ###
Nowadays, only the implementation of the first query of the DEBS 2015 Grand Challenge is implemented. If you want to contribute to this repository you are more than welcome. A contribution can be done to improve the actual implementation of Query 1 or to implement Query 2. 


## Reference ##
You can find further information about this application/benchmark on: TBC
