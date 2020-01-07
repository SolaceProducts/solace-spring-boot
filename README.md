# Solace Spring Boot

## Overview

An umbrella project containing all Solace projects for Spring Boot.

## Table of Contents
* [Maven Project Structure](#maven-project-structure)
* [Building Locally](#building-locally)
* [Running The Samples](#running-the-samples)
* [Contributing](#contributing)
* [Authors](#authors)
* [License](#license)
* [Support](#support)
* [Resources](#resources)

---

## Maven Project Structure

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

All sub-projects are included as modules of solace-spring-boot-build. Running `mvn package` or `mvn install` at the root of the project will install all sub-projects.

### Solace Spring Boot Projects

These are the projects which are contained within this repo:
* [Solace Spring Boot BOM](_docs/solace-spring-boot-bom.md)
* [Solace Java Spring Boot Starter](_docs/solace-java-spring-boot.md)
* [Solace JMS Spring Boot Starter](_docs/solace-jms-spring-boot.md)
* Solace Spring Boot Starter
  * This just combines Solace Java Spring Boot Starter and Solace JMS Spring Boot Starter.
* [Solace Java CF-Env](_docs/solace-java-cfenv.md)

### solace-spring-boot-build

*DO NOT USE THIS IN YOUR APPLICATION.*

This POM defines build-related plugins and profiles that are inherited by the BOM as well as for each of the sub-projects.

Please do not put non-Solace-Spring-Boot dependencies here - they belong in solace-spring-boot-parent. The exception to this is naturally the version of Spring Boot that this build targets.
If it shouldn't be inherited by the BOM, it doesn't go here.

### solace-spring-boot-parent

*DO NOT USE THIS IN YOUR APPLICATION.*

This POM defines common properties and dependencies for the Solace Spring Boot projects.

## Building Locally

To build the artifacts locally, simply clone this repository and run `mvn package` at the root of the project.
This will build everything.

```bash
git clone https://github.com/SolaceProducts/solace-spring-boot.git
cd solace-spring-boot
mvn package
```

If you want to install the latest versions of all the artifacts locally, you can also run a `mvn install`
```bash
git clone https://github.com/SolaceProducts/solace-spring-boot.git
cd solace-spring-boot
mvn install
```

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
    * For more information, consult the [Solace Java Spring Boot Starter README](_docs/solace-java-spring-boot.md#running-the-sample) or the [Solace JMS Spring Boot Starter README](_docs/solace-jms-spring-boot.md#running-the-sample).
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

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/SolaceProducts/solace-spring-boot/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v1.4%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

## Support

### Support Email
support@solace.com

### Solace Developer Community
https://solace.community

## Resources

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