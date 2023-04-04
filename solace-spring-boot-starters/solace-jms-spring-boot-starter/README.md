# Spring Boot Auto-Configuration for the Solace JMS

This project provides Spring Boot Auto-Configuration and an associated Spring Boot Starter for the Solace JMS API. The goal of this project is to make it easy to use Solace JMS within a Spring application so you can take advantage of all the benefits of Spring Boot auto-configuration.

For a high level introduction and explanation, you can also refer to the following blog post: https://solace.com/blog/devops/solace-jms-meet-spring-boot-starters

## Contents

* [Overview](#overview)
* [Using Auto-Configuration in your App](#using-auto-configuration-in-your-app)
* [Resources](#resources)

---

## Overview

As stated this project provides a Spring Boot Auto-Configuration implementation and a Spring Boot Starter pom for the Solace JMS API. The goal of this project is to make it easier to use Solace JMS within Spring.

The artifacts are published to Maven Central so it should be familiar and intuitive to use this project in your applications.


## Using Auto-Configuration in your App

Spring Boot Auto-Configuration for the Solace JMS supports both programmatic creation or JNDI lookup of JMS objects. To learn more about JNDI refer to the [Obtaining JMS objects using JNDI tutorial](//solace.com/samples/solace-samples-jms/using-jndi/).

#### Programmatic creation of JMS objects

See the associated [`solace-jms-sample-app`](../../solace-spring-boot-samples/solace-jms-sample-app) for an example of how this is all put together in a simple application. To use Solace JMS you need to do these steps:

1. [Update your build](#1-updating-your-build).
2. [Autowire](#2-autowiring-connection-objects) the `ConnectionFactory`:
3. [Configure the application](#3-configure-the-application-to-use-your-solace-pubsub-service-credentials) to use a Solace PubSub+ service.

#### JNDI lookup of JMS objects

See the associated [`solace-jms-sample-app-jndi`](../../solace-spring-boot-samples/solace-jms-sample-app-jndi) for an example. To use JNDI with Solace JMS you need to do these steps:

1. [Update your build](#1-updating-your-build).
2. [Autowire](#2-autowiring-connection-objects) the `JndiTemplate` for further use e.g.: in a `JndiObjectFactoryBean`.
3. [Configure the application](#3-configure-the-application-to-use-your-solace-pubsub-service-credentials) to use a Solace PubSub+ service.


### 1. Updating your build

The releases from this project are hosted in [Maven Central](//mvnrepository.com/artifact/com.solace.spring.boot/solace-jms-spring-boot-starter).

The easiest way to get started is to include the `solace-jms-spring-boot-starter` in your application. For an example see the [JMS Sample App](../../solace-spring-boot-samples/solace-jms-sample-app) in this project.

Here is how to include the latest spring boot starter in your project using Gradle and Maven. You can also add a specific version from [Maven Central](//mvnrepository.com/artifact/com.solace.spring.boot/solace-jms-spring-boot-starter ).
Note that you'll need to include version 3.1.0 or later to use Spring Boot release 2.x.

#### Using it with Gradle

```groovy
compile("com.solace.spring.boot:solace-jms-spring-boot-starter:5.0.0")
```

#### Using it with Maven

```xml
<dependency>
	<groupId>com.solace.spring.boot</groupId>
	<artifactId>solace-jms-spring-boot-starter</artifactId>
	<version>5.0.0</version>
</dependency>
```

### 2. Autowiring Connection Objects

To access the Solace message routing service, autowire the following connection objects in your code for JMS or JNDI:

```java
    @Autowired
    private ConnectionFactory connectionFactory;    // for JMS
```
```java
    @Autowired
    private JndiTemplate jndiTemplate;              // for JNDI
```

Note that if there are multiple services available, e.g. in a cloud deployment or if the application is configured by exposure of a [Solace PubSub+ service manifest](#exposing-a-solace-pubsub-service-manifest-in-the-applications-environment), one of the services will be picked automatically. You can control service selection by autowiring `com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory` or `com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory`, which enable getting the list of all services and use the Factory pattern to create a connection object.

### 3. Configure the Application to use your Solace PubSub+ Service Credentials

#### Deploying your Application to a Cloud Platform

By using [Solace Java CFEnv](../../solace-java-cfenv), this library can automatically configure a `ConnectionFactory` and/or a `JndiTemplate` using the detected Solace PubSub+ services when deployed on Cloud Foundry.

#### Exposing a Solace PubSub+ Service Manifest in the Application's Environment

Configuration of the `ConnectionFactory` and/or the `JndiTemplate` can be done through exposing a Solace PubSub+ service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace PubSub+ services in it and use them to accordingly configure your `JmsTemplate`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the [Manifest Load Order and Expected Formats](//github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) section in the [Solace Services Info](//github.com/SolaceProducts/solace-services-info) project.

#### Updating your Application Properties

Alternatively, configuration of the `JmsTemplate` can also be entirely done through the `application.properties` file located in the `src/main/resources` folder. This is where users can control the Solace JMS API properties. Currently this project supports direct configuration of the following properties:

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

## Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:

- [Spring Docs - Spring Boot Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](//github.com/snicoll-demos/spring-boot-master-auto-configuration)

For more information about Solace technology in general please visit these resources:

- The [Solace Developer Portal](//dev.solace.com)
- Understanding [Solace technology.](//dev.solace.com/tech/)
- Ask the [Solace community](//dev.solace.com/community/).
