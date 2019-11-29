[![Build Status](https://travis-ci.org/SolaceProducts/sl-spring-cloud-connectors.svg?branch=master)](https://travis-ci.org/SolaceProducts/sl-spring-cloud-connectors)

# Solace PubSub+ Spring Cloud Connectors

A Spring Cloud Connector for an instance of Solace PubSub+ in Cloud Foundry. Specifically a ServiceInfo and ServiceInfoCreator implementation for Solace PubSub+ in Cloud Foundry.

## Contents

* [Overview](#overview)
* [Spring Cloud Connectors](#spring-cloud-connectors)
* [Java Applications](#java-applications)
* [Spring Applications](#spring-applications)
* [Using it in your Application](#using-it-in-your-application)
* [Checking out and Building](#checking-out-and-building)
* [Contributing](#contributing)
* [Release Notes & Versioning](#release-notes-and-versioning)
* [Authors](#authors)
* [License](#license)
* [Resources](#resources)


---

## Overview

This project provides an implementation of the ServiceInfo and ServiceInfoCreator interfaces to extend the Spring Cloud Connectors project to the Solace PubSub+ Cloud Foundry service. Using this in your Spring application can make consuming the Solace PubSub+ service simpler than straight parsing of the `VCAP_SERVICES` environment variable.

The Spring cloud documentation provides both a nice introduction to Cloud Connectors and a nice overview of the options for [extending Spring Cloud](http://cloud.spring.io/spring-cloud-connectors/spring-cloud-connectors.html#_extending_spring_cloud_connectors). This project provides a Cloud Service Support extension to make it easy to consume the Solace PubSub+ Cloud Foundry Service in your Cloud Foundry application. The following diagram attempts to provide an architectural overview of what is implemented in this project.

![Architecture](resources/Architecture.png)

## Spring Cloud connectors

This project extends Spring Cloud Connectors. If you are new to Spring Cloud Connectors, check out their project on GitHub here: https://github.com/spring-cloud/spring-cloud-connectors

The following is a brief introduction copied from their README:

>Spring Cloud Connectors simplifies the process of connecting to services and gaining operating environment awareness in cloud platforms such as Cloud Foundry and Heroku, especially for Spring applications. It is designed for extensibility: you can use one of the provided cloud connectors or write one for your cloud platform, and you can use the built-in support for commonly-used services (relational databases, MongoDB, Redis, RabbitMQ) or extend Spring Cloud Connectors to work with your own services.

## Java Applications

Applications can use this connector with Spring Cloud to access the information in the VCAP_SERVICES environment variable, necessary for connection to a Solace PubSub+ Service Instance.

In the following example the code finds the Solace PubSub+ Cloud Foundry service instance name `MyService` and uses the `SolaceServiceCredentials` object to connect a Solace PubSub+ API for Java (JCSMP) session.

```java
CloudFactory cloudFactory = new CloudFactory();
Cloud cloud = cloudFactory.getCloud();
SolaceServiceCredentials solacemessaging = (SolaceServiceCredentials) cloud.getServiceInfo("MyService");

// Setting up the JCSMP Connection
final JCSMPProperties props = new JCSMPProperties();
props.setProperty(JCSMPProperties.HOST, solacemessaging.getSmfHost());
props.setProperty(JCSMPProperties.VPN_NAME, solacemessaging.getMsgVpnName());
props.setProperty(JCSMPProperties.USERNAME, solacemessaging.getClientUsername());
props.setProperty(JCSMPProperties.PASSWORD, solacemessaging.getClientPassword());

JCSMPSession session = JCSMPFactory.onlyInstance().createSession(props);
session.connect();
```

## Spring Applications

The Spring Cloud Auto-Configure Java, JMS and JNDI tutorials in the [Solace PubSub+ with Pivotal Cloud Foundry Getting Started Samples](https://dev.solace.com/samples/solace-samples-cloudfoundry-java/) provide easy integration into Spring applications.

Above example for the Solace PubSub+ API for Java (JCSMP) would be further simplified as follows: here Spring creates a SpringJCSMPFactory with all the properties set and all that is required is to autowire this into your application. Check out the [tutorial](https://dev.solace.com/samples/solace-samples-cloudfoundry-java/spring-cloud-autoconf-java/) for further details.

```java
@Autowired
private SpringJCSMPFactory solaceFactory;

JCSMPSession session = solaceFactory.createSession();
session.connect();
```

## Using it in your Application

The releases from this project are hosted in [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.solace.cloud.cloudfoundry%22%20AND%20a%3A%22solace-spring-cloud-connector%22)

##Here is how to include it in your project using Gradle and Maven.

Include version 4.0.0 or later to use Spring Boot release 2.x

### Using it with Gradle

```
// Solace Cloud
compile("com.solace.cloud.cloudfoundry:solace-spring-cloud-connector:4.1.0")
```

### Using it with Maven

```
<!-- Solace Cloud -->
<dependency>
  <groupId>com.solace.cloud.cloudfoundry</groupId>
  <artifactId>solace-spring-cloud-connector</artifactId>
  <version>4.1.0</version>
</dependency>
```

## Checking out and Building

This project depends on maven for building. To build the jar locally, check out the project and build from source by doing the following:

    git clone https://github.com/SolaceProducts/sl-spring-cloud-connectors.git
    cd sl-spring-cloud-connectors
    mvn package

This will build a jar file which will be named similar to the following:

```
target/solace-spring-cloud-connector-3.3.0-SNAPSHOT.jar
```

You can install this file in your maven repository locally.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Release Notes and Versioning

This project uses [SemVer](http://semver.org/) for versioning. For the versions available and corresponding release notes, see the [Releases in this repository](https://github.com/SolaceProducts/sl-spring-cloud-connectors/releases).

## Authors

See the list of [contributors](https://github.com/SolaceProducts/sl-spring-cloud-connectors/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Resources

For more information about Cloud Foundry and the Solace PubSub+ service these resources:
- [Solace PubSub+ for Pivotal Cloud Foundry](http://docs.pivotal.io/solace-messaging/)
- [Cloud Foundry Documentation](http://docs.cloudfoundry.org/)
- For an introduction to Cloud Foundry: https://www.cloudfoundry.org/

For more information about Spring Cloud try these resources:
- [Spring Cloud](http://projects.spring.io/spring-cloud/)
- [Spring Cloud Connectors](http://cloud.spring.io/spring-cloud-connectors/)
- [Spring Cloud Connectors Docs](http://cloud.spring.io/spring-cloud-connectors/spring-cloud-connectors.html)
- [Spring Cloud Connectors GitHub](https://github.com/spring-cloud/spring-cloud-connectors)

For more information about Solace technology in general please visit these resources:

- The Solace Developer Portal website at: http://dev.solacesystems.com
- Understanding [Solace technology.](http://dev.solacesystems.com/tech/)
- Ask the [Solace community](http://dev.solacesystems.com/community/).
