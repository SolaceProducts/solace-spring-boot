# Solace JMS Spring Boot Starter

## Overview

This is the Spring Boot starter for building Spring Boot applications with Solace which use JMS (Java Message Service).

## Table of Contents
* [Quickstart Guide](#quickstart-guide)
* [Configuration](#configuration)
    * [Exposing a Solace PubSub+ Service Manifest in the Application's Environment](#exposing-a-solace-pubsub-service-manifest-in-the-applications-environment)
    * [Updating your Application Properties](#updating-your-application-properties)

---

## Quickstart Guide

For information on how to get started quickly, see [here](../../README.md).

## Configuration

### Exposing a Solace PubSub+ Service Manifest in the Application's Environment

Configuration of the `ConnectionFactory` and/or the `JndiTemplate` can be done through exposing a Solace PubSub+ service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace PubSub+ services in it and use them to accordingly configure your `JmsTemplate`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the [Manifest Load Order and Expected Formats](//github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) section in the [Solace Services Info](//github.com/SolaceProducts/solace-services-info) project.

### Updating your Application Properties

Alternatively, configuration of the `JmsTemplate` can also be entirely done through the [`application.properties` file]([`application.properties` file](../../solace-spring-boot-samples/solace-jms-sample-app/src/main/resources/application.properties)) located in the `src/main/resources` folder. This is where users can control the Solace JMS API properties. Currently this project supports direct configuration of the following properties:

```
solace.jms.host
solace.jms.msgVpn
solace.jms.clientUsername
solace.jms.clientPassword
# Following properties do not apply when using JNDI, see below.
solace.jms.clientName
solace.jms.directTransport
```

Where reasonable, sensible defaults are always chosen. So a developer using a Solace PubSub+ message broker and wishing to use the default message-vpn may only set the `solace.jms.host`. When using JNDI, the configured connection factory properties on the Solace message broker are taken as a starting point, including the `clientName` and `directTransport` configurations.

See [`SolaceJmsProperties`](../../solace-spring-boot-autoconfigure/solace-jms-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJmsProperties.java) for the most up to date list of directly configurable properties.

Any additional supported Solace JMS API properties can be set through configuring `solace.jms.apiProperties.<Property>` where `<Property>` is the "Value" of the property in the ["com.solacesystems.jms.SupportedProperty" table as defined in the Solace JMS API documentation](//docs.solace.com/API-Developer-Online-Ref-Documentation/jms/constant-values.html#com.solacesystems.jms.SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE ), for example:

```
solace.jms.apiProperties.Solace_JMS_SSL_TrustStore=ABC
```

Note that the direct configuration of `solace.jms.` properties takes precedence over the `solace.jms.apiProperties.`.