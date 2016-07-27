# kafka-admin-api

Provides simple REST endpoints for listing and creating Kafka topics.

## About

This is a REST API for Kafka services administration on Trusted Analytics Platform.

This application utilizes a Kafka service broker (from TAP) and its client API to manage Kafka resources.

It performs basic operations, like:

* list all available topics
* create a topic
* read topic messages


## Configuring the application

The web service uses application.properties or application-cloud.properties to provide configuration data:

|key                                          | description                                  |
|---                                          |---                                           |
|kafka.brokersUri                             | A list of Kafka brokers                      |
|kafka.zookeeperUri                           | A list of all zookeeper brokers URI addresses|


To configure logging level the following property can be used:

    logging.level.org.trustedanalytics=DEBUG


## API

After deployment to TAP the Kafka Admin API provides the following endpoints:
  
|URL   	                |method  |operation                          |
|---	                |---     |---	                             |
|/api/topics   	        |GET     |list the topics   	             |
|/api/topics   	        |POST    |create a new topic   	             |
|/api/topics/{name}     |GET     |read topic messages                |
|/api/topics/{name}     |POST    |write plain text message to a topic|


## Swagger UI

You can work with the API via the SwaggerUI.

Just go to the following example URL and use the Swagger functionality:

    http://kafka-admin-api.{domain.com}/swagger-ui.html


## Deploying to Cloud Foundry

### Manual deployment
To deploy this application to Cloud Foundry you could use the following manifest file (after putting your own configuration data):

    ---
    applications:
    - name: kafka-admin-api
      memory: 512MB
      instances: 1
      host: kafka-admin-api
      path: target/kafka-admin-api-0.1.0.jar
      services:
      - kafka-instance

Notes:
* Executing any *mvn clean* or *mvn package* will delete or override any changes to manifest.yml respectively.
* Replacing "kafka-instance" service binding to some other service requires updating **application-cloud.properties**: 
   
        kafka.brokerUri=${vcap.services.YOUR-KAFKA-INSTANCE.credentials.uri}
        kafka.zookeeperUri=${vcap.services.YOUR-KAFKA-INSTANCE.credentials.zookeeperUri}


### Automated deployment

* Switch to `deploy` directory: `cd deploy`
* Install tox: `sudo -E pip install --upgrade tox`
* Run: `tox`
* Activate virtualenv with installed dependencies: `. .tox/py27/bin/activate`
* Run deployment script: `python deploy.py` providing required parameters when running script (`python deploy.py -h` to check script parameters with their descriptions).


## Local development

For local development Zookeeper and Kafka must be installed.

### Zookeeper

Zookeeper can be downloaded from Apache ZooKeeper Releases

I assume you are working on Linux machine and will be using kafka-admin-api directory as a workspace.

Simple installation could look like this:

    cd ~/kafka-admin-api
    mkdir zookeeper
    cd zookeeper
    wget http://apache.mirrors.lucidnetworks.net/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
    tar -xvf zookeeper-3.4.6.tar.gz
    cd zookeeper-3.4.6/

To test the installation:

    cp conf/zoo_sample.cfg conf/zoo.cfg
    bin/zkServer.sh start

You should see something similar to:

    JMX enabled by default
    Using config: /home/ubuntu/zookeeper-3.4.6/bin/../conf/zoo.cfg
    Starting zookeeper ... STARTED

### Kafka

Kafka downloads ara available here: http://kafka.apache.org/downloads.html. The current stable version is 0.10.0.0.

I pick version kafka_2.11-0.10.0.0.tgz from the following mirror: http://mirrors.sonic.net/apache/kafka/0.10.0.0/kafka_2.11-0.10.0.0.tgz

    cd ~/kafka-admin-api
    mkdir kafka
    cd kafka
    wget http://mirrors.sonic.net/apache/kafka/0.10.0.0/kafka_2.11-0.10.0.0.tgz
    tar xvzf kafka_2.11-0.10.0.0.tgz  

To test if it works, start the server:

    cd kafka_2.11-0.10.0.0
    bin/kafka-server-start.sh config/server.properties


### Building and running

To run this API locally you can execute the following commands:

    mvn clean package
    mvn spring-boot:run


### Testing

For testing you can use SwaggerUI or curl.
 
* Swagger UI is available here:

    http://kafka-admin-api.{domain.com}/api/topics

    
* To list topics invoke below curl:

    curl http://kafka-admin-api.{domain.com}/api/topics


* To create a topic invoke below curl:

    curl -H "Content-Type: application/json" -X POST -d '{"topic":"test_topic_1"}' http://kafka-admin-api.{domain.com}/api/topics
    Note: The default partitions number in this case is 2.

* To create a topic with an explicit partitions number use this:

    curl -H "Content-Type: application/json" -X POST -d '{"topic":"test_topic_1","partitions":2}' http://kafka-admin-api.{domain.com}/api/topics

* To read topic messages use this:

    curl http://kafka-admin-api.{domain.com}/api/topics/{__TOPIC_NAME__}

    Here you have to provide the domain and TOPIC_NAME.

* To write a message to a topic use this:

    curl -H "Content-Type: text/plain" -X POST -d 'my test message' http://kafka-admin-api.{domain.com}/api/topics


## Future improvements

* new operation: delete topic 
* performance improvement: Zookeeper connection handling
* exception handling: zookeeper connection lost during service startup
