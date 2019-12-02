# Solace Java Spring Boot Starter

## About

This is the Spring Boot starter for building Spring Boot applications with Solace which use the Solace Messaging API for Java (JCSMP).

## Table of Contents
* [Quickstart Guide](#quickstart-guide)
* [Configuration](#configuration)
    * [Exposing a Solace PubSub+ Service Manifest in the Application's Environment](#exposing-a-solace-pubsub-service-manifest-in-the-applications-environment)
    * [Updating your Application Properties](#updating-your-application-properties)

---

## Quickstart Guide

To get started quickly, see [here](../../README.md)

## Configuration

### Exposing a Solace PubSub+ Service Manifest in the Application's Environment

Configuration of the `SpringJCSMPFactory` can be done through exposing a Solace PubSub+ service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace PubSub+ services in it and use them to accordingly configure your `SpringJCSMPFactory`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the _Manifest Load Order and Expected Formats_ section in the [Solace Services Info](https://github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) project.

### Updating your Application Properties

Alternatively, configuration of the `SpringJCSMPFactory` can also be done through the [`application.properties` file](../../solace-spring-boot-samples/solace-java-sample-app/src/main/resources/application.properties). This is where users can control the Solace Java API properties. Currently this project supports direct configuration of the following properties:

```
solace.java.host
solace.java.msgVpn
solace.java.clientUsername
solace.java.clientPassword
solace.java.clientName
solace.java.connectRetries
solace.java.reconnectRetries
solace.java.connectRetriesPerHost
solace.java.reconnectRetryWaitInMillis
```

Where reasonable, sensible defaults are always chosen. So a developer using a Solace PubSub+ message broker and wishing to use the default message-vpn may only set the `solace.java.host`.

See [`SolaceJavaProperties`](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJavaProperties.java) for the most up to date list.

Any additional Solace Java API properties can be set through configuring `solace.java.apiProperties.<Property>` where `<Property>` is the name of the property as defined in the [Solace Java API documentation for `com.solacesystems.jcsmp.JCSMPProperties`](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/constant-values.html#com.solacesystems.jcsmp.JCSMPProperties.ACK_EVENT_MODE), for example:

```
solace.java.apiProperties.reapply_subscriptions=false
solace.java.apiProperties.ssl_trust_store=/path/to/truststore
```

Note that the direct configuration of `solace.java.` properties takes precedence over the `solace.java.apiProperties.`.