# Solace Spring Boot

## Overview

This project includes and builds all the Solace starters for Spring Boot.
This includes the Java (JCSMP) starter and the JMS starter.

## Table of contents
* [Quickstart Guide](#quickstart-guide)
    * [Quickstart Guide - Java](#quickstart-guide---java)
    * [Quickstart Guide - JMS](#quickstart-guide---jms)
* [Advanced Configuration](#advanced-configuration)
    * [Advanced Configuration - Java](#advanced-configuration---java)
    * [Advanced Configuration - JMS](#advanced-configuration---jms)
* [Building Locally](#building-locally)
    * [Maven Project Structure](#maven-project-structure)
* [Running The Samples](#running-the-samples)
    * [What Do The Samples Do?](#what-do-the-samples-do)
* [Additional Information](#additional-information)
    * [Contributing](#contributing)
    * [Authors](#authors)
    * [License](#license)
    * [Support](#support)
    * [Resources](#resources)

## Quickstart Guide

To get started, we need to pull in 2 dependencies:
1. `solace-spring-boot-starter` (includes both `solace-java-spring-boot-starter` and `solace-jms-spring-boot-starter`)
2. `solace-spring-boot-bom`

Once these dependencies are declared, we can automatically autowire Solace Spring Boot beans.

### Quickstart Guide - JMS

1. Import `solace-spring-boot-starter` for the build tool you are using:
    * [Using Maven](#maven-quickstart)
    * [Using Gradle 4](#gradle-4-quickstart)
    * [Using Gradle 5](#gradle-5-quickstart)

2. Autowire the following connection objects in your code for JMS or JNDI:

    ```java
        @Autowired
        private ConnectionFactory connectionFactory;    // for JMS
    ```
    ```java
        @Autowired
        private JndiTemplate jndiTemplate;              // for JNDI
    ```
    
    Note that if there are multiple services available, e.g. in a cloud deployment or if the application is configured by exposure of a [Solace PubSub+ service manifest](#jms---exposing-a-solace-pubsub-service-manifest-in-the-applications-environment), one of the services will be picked automatically. You can control service selection by autowiring `com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory` or `com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory`, which enable getting the list of all services and use the Factory pattern to create a connection object.

### Quickstart Guide - Java

1. Import `solace-spring-boot-starter` for the build tool you are using:
    * [Using Maven](#maven-quickstart)
    * [Using Gradle 4](#gradle-4-quickstart)
    * [Using Gradle 5](#gradle-5-quickstart)

2. Declare `SpringJCSMPFactory` and annotate it so that it is autowired:
    
    ```java
    @Autowired
    private SpringJCSMPFactory solaceFactory;
    ```
    
    Once you have the `SpringJCSMPFactory`, it behaves just like the `JCSMPFactory` and can be used to create sessions. For example:
    
    ```java
    final JCSMPSession session = solaceFactory.createSession();
    ```
    
    The `SpringJCSMPFactory` is a wrapper of the singleton `JCSMPFactory` which contains an associated `JCSMPProperties`. This facilitates auto-wiring by Spring but otherwise maintains the familiar `JCSMPFactory` interface known to users of the Solace Java API.
    
    Alternatively, you could autowire one or more of the following to create your own customized `SpringJCSMPFactory`:
    
    ```java
    /* A factory for creating SpringJCSMPFactory. */
    @Autowired
    private SpringJCSMPFactoryCloudFactory springJcsmpFactoryCloudFactory;
    
    /* A POJO describing the credentials for the first detected Solace PubSub+ service */
    @Autowired
    private SolaceServiceCredentials solaceServiceCredentials;
    
    /* The properties of a JCSMP connection for the first detected Solace PubSub+ service */
    @Autowired
    private JCSMPProperties jcsmpProperties;
    ```
    
    However note that the `SolaceServiceCredentials` will only provide meaningful information if the application is configured by [exposure of a Solace PubSub+ service manifest](#java---exposing-a-solace-pubsub-service-manifest-in-the-applications-environment), and not by using the [application properties file](#java---updating-your-application-properties).

#### Maven Quickstart
```xml
    <!-- Add me to your POM.xml -->
    <properties>
        <spring.boot.version>2.2.0.RELEASE</spring.boot.version>
    </properties>

    <dependencyManagement>
        <groupId>com.solace.spring.boot</groupId>
        <artifactId>solace-spring-boot-bom</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>com.solace.spring.boot</groupId>
            <artifactId>solace-spring-boot-starter</artifactId>    
        </dependency>
    </dependencies>
```

#### Gradle 4 Quickstart
```groovy
    /* Add me to your build.gradle */
    buildscript {
        ext {
            springBootVersion = '2.2.0.RELEASE'
        }
        dependencies {
            classpath 'io.spring.gradle:dependency-management-plugin:1.0.8.RELEASE'
        }
    }

    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom "com.solace.spring.boot:solace-spring-boot-bom:${springBootVersion}"
        }
    }
    
    dependencies {
        compile("com.solace.spring.boot:solace-spring-boot-starter")
    }
```

Note: Gradle 4 isn't natively compatible with Maven BOM's. Thus, we have to use the Spring's dependency management plugin.

#### Gradle 5 Quickstart
```groovy
    /* Add me to your build.gradle */
    buildscript {
        ext {
            springBootVersion = '2.2.0.RELEASE'
        }
    }
    
    dependencies {
        implementation(platform("com.solace.spring.boot:solace-spring-boot-bom:${springBootVersion}"))
        implementation("com.solace.spring.boot:solace-spring-boot-starter")
    }
```

## Advanced Configuration

### Advanced Configuration - Java

#### Java - Exposing a Solace PubSub+ Service Manifest in the Application's Environment

Configuration of the `SpringJCSMPFactory` can be done through exposing a Solace PubSub+ service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace PubSub+ services in it and use them to accordingly configure your `SpringJCSMPFactory`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#java---updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the _Manifest Load Order and Expected Formats_ section in the [Solace Services Info](https://github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) project.

#### Java - Updating your Application Properties

Alternatively, configuration of the `SpringJCSMPFactory` can also be done through the [`application.properties` file](https://github.com/SolaceProducts/solace-spring/blob/master/solace-spring-boot-samples/solace-java-sample-app/src/main/resources/application.properties). This is where users can control the Solace Java API properties. Currently this project supports direct configuration of the following properties:

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

See [`SolaceJavaProperties`](https://github.com/SolaceProducts/solace-spring/blob/master/solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJavaProperties.java) for the most up to date list.

Any additional Solace Java API properties can be set through configuring `solace.java.apiProperties.<Property>` where `<Property>` is the name of the property as defined in the [Solace Java API documentation for `com.solacesystems.jcsmp.JCSMPProperties`](https://docs.solace.com/API-Developer-Online-Ref-Documentation/java/constant-values.html#com.solacesystems.jcsmp.JCSMPProperties.ACK_EVENT_MODE), for example:

```
solace.java.apiProperties.reapply_subscriptions=false
solace.java.apiProperties.ssl_trust_store=/path/to/truststore
```

Note that the direct configuration of `solace.java.` properties takes precedence over the `solace.java.apiProperties.`.

### Advanced Configuration - JMS

#### JMS - Exposing a Solace PubSub+ Service Manifest in the Application's Environment

Configuration of the `ConnectionFactory` and/or the `JndiTemplate` can be done through exposing a Solace PubSub+ service manifest to the application's JVM properties or OS environment.

For example, you can set a `SOLCAP_SERVICES` variable in either your JVM properties or OS's environment to directly contain a `VCAP_SERVICES`-formatted manifest file. In which case, the autoconfigure will pick up any Solace PubSub+ services in it and use them to accordingly configure your `JmsTemplate`.

The properties provided by this externally-provided manifest can also be augmented using the values from the [application's properties file](#jms---updating-your-application-properties).

For details on valid manifest formats and other ways of exposing Solace service manifests to your application, see the [Manifest Load Order and Expected Formats](//github.com/SolaceProducts/solace-services-info#manifest-load-order-and-expected-formats) section in the [Solace Services Info](//github.com/SolaceProducts/solace-services-info) project.

#### JMS - Updating your Application Properties

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

See [`SolaceJmsProperties`](//github.com/SolaceProducts/solace-spring/blob/master/solace-spring-boot-autoconfigure/solace-jms-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJmsProperties.java) for the most up to date list of directly configurable properties.

Any additional supported Solace JMS API properties can be set through configuring `solace.jms.apiProperties.<Property>` where `<Property>` is the "Value" of the property in the ["com.solacesystems.jms.SupportedProperty" table as defined in the Solace JMS API documentation](//docs.solace.com/API-Developer-Online-Ref-Documentation/jms/constant-values.html#com.solacesystems.jms.SupportedProperty.SOLACE_JMS_SSL_TRUST_STORE ), for example:

```
solace.jms.apiProperties.Solace_JMS_SSL_TrustStore=ABC
```

Note that the direct configuration of `solace.jms.` properties takes precedence over the `solace.jms.apiProperties.`.


## Building Locally

To build the artifacts locally, run `mvn package` at the root of the project.
This will build everything.

```bash
git clone https://github.com/SolaceProducts/solace-spring.git
cd solace-spring
mvn package
```

### Maven Project Structure

```
solace-spring-boot-build (root)
<-- solace-spring-boot-bom
<-- solace-spring-boot-parent 
    <-- solace-spring-boot-java-starter
    <-- solace-spring-boot-jms-starter
    <-- solace-spring-boot-starter
    <-- solace-java-spring-boot-autoconfigure
    <-- solace-jms-spring-boot-autoconfigure

solace-java-sample-app
solace-jms-sample-app
solace-jms-sample-app-jndi

Where <-- indicates the parent of the project
```

All subprojects are included as modules of solace-spring-boot-build. Running `mvn install` at the root of the project will install all subprojects.

#### solace-spring-boot-build

This POM defines build-related plugins and profiles that are inherited by the BOM as well as the starters and autoconfiguration.
The version of this POM should match the version of Spring Boot that the build will target.

Please do not put dependency related properties here - they belong in solace-spring-boot-parent. The exceptions to this, naturally, are the versions of the Solace starters as well as the version of Spring Boot this build targets.
If it shouldn't be inherited by the BOM, it doesn't go here.

#### solace-spring-boot-parent

This POM defines common properties and dependencies for the Spring Boot starters and autoconfigurations. 

If a starter or autoconfiguration shares a dependency with another starter, it is a good idea to specify it as a property in this POM to keep things tidy. It would not be beneficial to have two versions of a common library be included in the starter if a common version works with both.

#### solace-spring-boot-bom

The BOM (Bill of Materials) defines the exact version of starters to use for a specific version of Spring Boot. This is done to ensure compatibility with that specific version of Spring Boot, and to make version management easier for Spring Boot applications.

#### solace-spring-boot-starter, solace-java-spring-boot-starter, solace-jms-spring-boot-starter

Spring Boot starters. They include their respective autoconfiguration as a dependency.

Note that solace-spring-boot-starter is simply both the Java and JMS starter bundled together. This is done so that a single dependency can be listed for the Spring Initialzr (https://start.spring.io).

#### solace-java-spring-boot-autoconfigure, solace-jms-spring-boot-autoconfigure

Spring Boot autoconfigurations.

#### solace-java-sample-app, solace-jms-sample-app, solace-jms-sample-app-jndi

These samples demonstrate a basic use of the Solace Spring Boot starters. See [Running The Samples](#running-the-samples) below.

## Running The Samples

To run the samples you will need a Solace PubSub+ Event Broker.
Here are two ways you can get started quickly if you don't already have a PubSub+ instance:

1. Get a free Solace PubSub+ event broker cloud instance
    * Visit https://solace.com/products/event-broker/cloud/
    * Create an account and instance for free
2. Run the Solace PubSub+ event broker locally
    * Visit https://solace.com/downloads/
    * A variety of download options are available to run the software locally
    * Follow the instructions for whatever download option you choose
    
Now, once you have your PubSub+ instance, you will just need to do these two things:

1. Configure `application.properties`
    * This file can be found in `src/main/resources`
    * For more information, see [Updating your application properties (Java)](#java---updating-your-application-properties) or [Updating your application properties (JMS)](#jms---updating-your-application-properties)
    * Example: 
       ```
       solace.java.host=tcp://192.168.133.64:55555
       solace.java.msgVpn=default
       solace.java.clientUsername=yourUsername
       solace.java.clientPassword=yourPassword
       ```
2. Run the project from the root folder of the sample using Maven
    ```bash
    cd solace-java-sample-app
    mvn spring-boot:run
    ```

### What Do The Samples Do?

The samples work by publishing a simple message of "Hello World" to the event broker. Then, because they are subscribed to the topic that they published "Hello World" to, they should also receive "Hello World" back. You can watch this happen by looking at what is logged to the console when you run `mvn spring-boot:run`.

## Additional Information

### Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on the process for submitting pull requests to us.

### Authors

See the list of [contributors](https://github.com/SolaceProducts/solace-spring/graphs/contributors) who participated in this project.

### License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

### Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v1.4%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

### Support

#### Support Email
support@solace.com

#### Solace Developer Community
https://solace.community

### Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:

- [Spring Docs - Spring Boot Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](//github.com/snicoll-demos/spring-boot-master-auto-configuration)

For more information about Solace technology in general please visit these resources:

- The Solace Developer Portal website at: https://solace.dev

```
.......................HELLO FROM THE OTTER SIDE...........
............................www.solace.com.................
...........................................................
...........................@@@@@@@@@@@@@@@@@@@.............
........................@@                    @@...........
.....................@      #              #     @.........
....................@       #              #      @........
.....................@          @@@@@@@@@        @.........
......................@        @@@@@@@@@@@      @..........
.....................@           @@@@@@@         @.........
.....................@              @            @.........
.....................@    \_______/   \________/ @.........
......................@         |       |        @.........
.......................@        |       |       @..........
.......................@         \_____/       @...........
....@@@@@...............@                      @...........
..@@     @...............@                     @...........
..@       @@.............@                     @...........
..@        @@............@                     @...........
..@@        @............@                     @...........
....@       @............@                      @..........
.....@@     @...........@                        @.........
.......@     @.........@                          @........
........@     @........@                           @.......
........@       @@@@@@                              @......
.........@                                            @....
.........@                                             @...
..........@@                                           @...
............@                                          @...
.............@                              @          @...
...............@                             @         @...
.................@                            @        @...
..................@                            @       @...
...................@                           @       @...
...................@                           @       @...
...................@                          @        @...
...................@                         @        @....
..................@                         @         @....
..................@                        @         @.....
..................@                       @          @.....
..................@                       @         @@.....
..................@                        @       @ @.....
..................@                          @@@@@   @.....
..................@                                  @.....
..................@                                  @.....
```