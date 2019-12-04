# Solace Spring Boot

## Overview

This project includes and builds all the Solace starters for Spring Boot.
This includes the Java (JCSMP) starter and the JMS starter.

## Table of contents
* [Quickstart Guide](#quickstart-guide)
    * [Quickstart Guide - Spring Boot Version Compatibility](#quickstart-guide---spring-boot-version-compatibility)
    * [Quickstart Guide - Java](#quickstart-guide---java)
    * [Quickstart Guide - JMS](#quickstart-guide---jms)
* [Building Locally](#building-locally)
    * [Maven Project Structure](#maven-project-structure)
* [Running The Samples](#running-the-samples)
    * [What Do The Samples Do?](#what-do-the-samples-do)
* [Additional Information](#additional-information)
    * [Solace Java Spring Boot Starter](#solace-java-spring-boot-starter-readme)
    * [Solace JMS Spring Boot Starter](#solace-jms-spring-boot-starter-readme)
    * [Solace Java CFEnv](#solace-java-cfenv-readme)
* [Additional Meta-Information](#additional-meta-information)
    * [Contributing](#contributing)
    * [Authors](#authors)
    * [License](#license)
    * [Support](#support)
    * [Resources](#resources)
    
---



## Quickstart Guide

To get started, we need to pull in 2 dependencies:
1. `solace-spring-boot-starter` (includes both `solace-java-spring-boot-starter` and `solace-jms-spring-boot-starter`)
2. `solace-spring-boot-bom`

Once these dependencies are declared, we can automatically autowire Solace Spring Boot beans.

### Quickstart Guide - Spring Boot Version Compatibility

The `solace-spring-boot-bom` will guarantee that the versions of the Solace Spring Boot starters and autoconfigurations are what works with your version of Spring Boot.
Consult the table below to determine what version of the BOM you need for your version of Spring Boot.

|Spring Boot       | Solace Spring Boot BOM |
|----------------- |------------------------|
| 2.2.0            | 1.0.0                  |
| 2.2.1            |                        |
| 2.2.2-SNAPSHOT   |                        |

### Quickstart Guide - JMS

1. Import `solace-spring-boot-starter` for the build tool you are using:
    * [Using Maven](#maven-quickstart)
    * [Using Gradle 4](#gradle-4-quickstart)
    * [Using Gradle 5](#gradle-5-quickstart)
2.Configure your properties file. For more detailed information, see the [Solace JMS Spring Boot Starter README](solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md).
  ```properties
  # Add me to your application.properties
  solace.jms.host=tcp://mrabcdef123.messaging.solace.cloud:20032
  solace.jms.msgVpn=msgvpn-abcdef123
  solace.jms.clientUsername=solace-cloud-client
  solace.jms.clientPassword=hunter2
  ```
You can skip this step if you are running within Cloud Foundry as local services will be discovered automatically by default.

3. Autowire the following connection objects in your code for JMS or JNDI:

    ```java
        @Autowired
        private ConnectionFactory connectionFactory;    // for JMS
    ```
    ```java
        @Autowired
        private JndiTemplate jndiTemplate;              // for JNDI
    ```
    
    Note that if there are multiple services available, e.g. in a cloud deployment or if the application is configured by exposure of a [Solace PubSub+ service manifest](solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md), one of the services will be picked automatically. You can control service selection by autowiring `com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory` or `com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory`, which enable getting the list of all services and use the Factory pattern to create a connection object.

### Quickstart Guide - Java

1. Import `solace-spring-boot-starter` for the build tool you are using:
    * [Using Maven](#maven-quickstart)
    * [Using Gradle 4](#gradle-4-quickstart)
    * [Using Gradle 5](#gradle-5-quickstart)
2. Configure your properties file. For more detailed information, see the [Solace Java Spring Boot Starter README](solace-spring-boot-starters/solace-java-spring-boot-starter/README.md).
  ```properties
  # Add me to your application.properties
  solace.java.host=tcp://mrabcdef123.messaging.solace.cloud:20032
  solace.java.msgVpn=msgvpn-abcdef123
  solace.java.clientUsername=solace-cloud-client
  solace.java.clientPassword=hunter2
  ```
You can skip this step if you are running within Cloud Foundry as local services will be discovered automatically by default.
3. Declare `SpringJCSMPFactory` and annotate it so that it is autowired:
    
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
    
    However note that the `SolaceServiceCredentials` will only provide meaningful information if the application is configured by [exposure of a Solace PubSub+ service manifest](solace-spring-boot-starters/solace-java-spring-boot-starter/README.md), and not by using the [application properties file](solace-spring-boot-starters/solace-java-spring-boot-starter/README.md).

#### Maven Quickstart
```xml
    <!-- Add me to your POM.xml -->
    <properties>
        <spring.boot.version>2.2.0.RELEASE</spring.boot.version>
    
        <!-- Consult the README versioning table -->
        <solace.spring.boot.bom.version>1.0.0</solace.spring.boot.bom.version>
    </properties>

    <dependencyManagement>
        <groupId>com.solace.spring.boot</groupId>
        <artifactId>solace-spring-boot-bom</artifactId>
        <version>${solace.spring.boot.bom.version}</version>
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

            // Consult the README versioning table
            solaceSpringBootBomVersion = '1.0.0'
        }
        dependencies {
            classpath 'io.spring.gradle:dependency-management-plugin:1.0.8.RELEASE'
        }
    }

    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom "com.solace.spring.boot:solace-spring-boot-bom:${solaceSpringBootBomVersion}"
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

            // Consult the README versioning table
            solaceSpringBootBomVersion = '1.0.0'
        }
    }
    
    dependencies {
        implementation(platform("com.solace.spring.boot:solace-spring-boot-bom:${solaceSpringBootBomVersion}"))
        implementation("com.solace.spring.boot:solace-spring-boot-starter")
    }
```

## Building Locally

To build the artifacts locally, simply clone this repository and run `mvn package` at the root of the project.
This will build everything.

```bash
git clone https://github.com/SolaceProducts/solace-spring-boot.git
cd solace-spring-boot
mvn package
```

If you want to install the latest versions of all the artifacts locally, you can also run a 'mvn install'
```bash
git clone https://github.com/SolaceProducts/solace-spring-boot.git
cd solace-spring-boot
mvn install
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
    <-- solace-java-cfenv

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

#### solace-java-cfenv

This subproject loads credentials for local Solace services. It is used for when you are running within a Cloud Foundry environment.

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
    * For more information, see the [Solace Java Spring Boot Starter README](solace-spring-boot-starters/solace-java-spring-boot-starter/README.md) or the [Solace JMS Spring Boot Starter README](solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md)
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

You can find additional information about each of the projects in their respective README's.

### Solace Java Spring Boot Starter README

[solace-spring-boot-starters/solace-java-spring-boot-starter/README.md](solace-spring-boot-starters/solace-java-spring-boot-starter/README.md)

### Solace JMS Spring Boot Starter README

[solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md](solace-spring-boot-starters/solace-jms-spring-boot-starter/README.md)

### Solace Java CFEnv README

[solace-spring-boot/solace-java-cfenv/README.md](solace-spring-boot/solace-java-cfenv/README.md)

## Additional Meta-Information

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