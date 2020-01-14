# Solace Spring Boot Sample - Java

This is a simple sample project to demonstrate the autoconfiguration of the Solace Java API using the `solace-spring-boot-starter` (or the `solace-java-spring-boot-starter` in particular).

Please refer to the [Spring Boot Auto-Configuration for the Solace Java API](../../solace-spring-boot-starters/solace-java-spring-boot-starter) project for more detail.

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

Please consult the [Spring Boot Auto-Configuration for the Solace Java API](../../solace-spring-boot-starters/solace-java-spring-boot-starter/README.md#configure-the-application-to-use-your-solace-pubsub-service-credentials) documentation for the details on how to connect this sample to a Solace PubSub+ service.

## Running the Sample

The simplest way to run the sample is from the project root folder using maven. For example:

```shell script
cd solace-spring-boot-samples/solace-java-sample-app
mvn spring-boot:run
```

Hint: look for "Sending Hello World" and "TextMessage received: Hello World" in the displayed logs.