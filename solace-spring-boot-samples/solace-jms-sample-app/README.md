# Solace Spring Boot Sample - JMS

This is a simple sample project to demonstrate the autoconfiguration of the Solace JMS API using the `solace-spring-boot-starter` (or the `solace-jms-spring-boot-starter` in particular).

Please refer to the [Spring Boot Auto-Configuration for the Solace JMS API](../../solace-spring-boot-starters/solace-jms-spring-boot-starter) project for more detail.

## Contents

* [Acquiring a Solace PubSub+ Service](#acquiring-a-solace-pubsub-service)
* [Configuring the Application to use your Solace PubSub+ Service Credentials](#configuring-the-sample-to-use-your-solace-pubsub-service-credentials)
* [Running the Sample](#running-the-sample)

## Acquiring a Solace PubSub+ Service

To run the samples you will need a Solace PubSub+ Event Broker.
Here are two ways to quickly get started if you don't already have a PubSub+ instance:

1. Get a free Solace PubSub+ event broker cloud instance
    * Visit https://solace.com/products/event-broker/cloud/
    * Create an account and instance for free
2. Run the Solace PubSub+ event broker locally
    * Visit https://solace.com/downloads/
    * A variety of download options are available to run the software locally
    * Follow the instructions for whatever download option you choose

## Configuring the Sample to use your Solace PubSub+ Service Credentials

Please consult the [Spring Boot Auto-Configuration for the Solace JMS API](../../solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md#3-configure-the-application-to-use-your-solace-pubsub-service-credentials) documentation for the details on how to connect this sample to a Solace PubSub+ service.

## Running the Sample

The simplest way to run the sample is from the project root folder using maven. For example:

```shell script
cd solace-spring-boot-samples/solace-jms-sample-app
mvn spring-boot:run
```

Note: the JMS sample will automatically provision the queue used for testing on the message broker.

### Troubleshooting Tips

The sample is logging to the console by default. This can be adjusted in the [`log4j2.xml`](./src/main/resources/log4j2.xml) log file provided in the `src/main/resources` folder.

Solace API logging can be enabled and configured in the [`application.properties`](./src/main/resources/application.properties) file located in the same folder, by adding:

```properties
# Solace logging example:
logging.level.com.solacesystems=INFO
```