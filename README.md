[![build](https://github.com/SolaceProducts/solace-spring-boot/actions/workflows/build-test.yml/badge.svg)](https://github.com/SolaceProducts/solace-spring-boot/actions/workflows/build-test.yml)

# Solace Spring Boot

## Overview

An umbrella project containing all Solace projects for Spring Boot.

For Solace projects on Spring Cloud, please visit the [Solace Spring Cloud](//github.com/SolaceProducts/solace-spring-cloud) project.

## Table of Contents
* [Repository Contents](#repository-contents)
* [Building Locally](#building-locally)
* [Release Process](#release-process)
* [Contributing](#contributing)
* [Authors](#authors)
* [License](#license)
* [Support](#support)
* [Resources](#resources)

---

## Repository Contents

> Note:
> 
> The [master](//github.com/SolaceProducts/solace-spring-boot/tree/master) branch dependencies have been updated to the **Spring Boot 3.x** which requires **JDK 17** or higher.
>
> The [master-2.x](//github.com/SolaceProducts/solace-spring-boot/tree/master-2.x) branch will act as master branch for solace-spring-boot compatible with **Spring Boot 2.x**.


### Solace Spring Boot Bill of Materials (BOM)

The [Solace Spring Boot BOM](./solace-spring-boot-bom) is a POM file which defines the versions of [Solace Spring Boot projects](#solace-spring-boot-projects) that are compatible to a particular version of Spring Boot.

Please consult the [Spring Boot Compatibility Table](./solace-spring-boot-bom/README.md#spring-boot-version-compatibility) to determine which version of the BOM is compatible with your project. 

### Solace Spring Boot Projects

These are the projects contained within this repository:
* [Solace Java Spring Boot Starter](./solace-spring-boot-starters/solace-java-spring-boot-starter)
* [Solace JMS Spring Boot Starter](./solace-spring-boot-starters/solace-jms-spring-boot-starter)
* [Solace Spring Boot Starter](./solace-spring-boot-starters/solace-spring-boot-starter)

### Solace Spring Boot Sample Applications

The sample applications for all Solace Spring Boot projects can be found under [solace-spring-boot-samples](./solace-spring-boot-samples).

## Building Locally

To build the artifacts locally, simply clone this repository and run `mvn package` at the root of the project.
This will build everything.

```shell script
git clone https://github.com/SolaceProducts/solace-spring-boot.git
cd solace-spring-boot
mvn package # or mvn install to install them locally
```

### Maven Project Structure

```
solace-spring-boot-build (root)
    <-> solace-spring-boot-bom
    <-> solace-spring-boot-parent
        <-> solace-spring-boot-java-starter
        <-> solace-spring-boot-jms-starter
        <-> solace-spring-boot-starter
        <-> solace-java-spring-boot-autoconfigure
        <-> solace-jms-spring-boot-autoconfigure
    --> solace-java-sample-app
    --> solace-jms-sample-app
    --> solace-jms-sample-app-jndi
    --> solace-java-oauth2-sample-app

Where:
    <-- indicates the parent of the project
    --> indicates a sub-module of the project
```

All sub-projects are included as modules of `solace-spring-boot-build`. Running `mvn package` or `mvn install` at the root of the project will package/install all sub-projects.

#### Build Projects

These projects are used to build the `solace-spring-boot` repository. They should not be used in your actual application.

- solace-spring-boot-build  
This POM defines build-related plugins and profiles that are inherited by the BOM as well as for each of the sub-projects.  
Please do not put non-Solace-Spring-Boot dependencies here - they belong in solace-spring-boot-parent. The exception to this is naturally the version of Spring Boot that this build targets.
If it shouldn't be inherited by the BOM, it doesn't go here.
- solace-spring-boot-parent  
This POM defines common properties and dependencies for the Solace Spring Boot projects.

## Release Process

 ```shell script
 mvn -B release:prepare release:perform
 ```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on the process for submitting pull requests to us.

## Authors

See the list of [contributors](//github.com/SolaceProducts/solace-spring-boot/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v1.4%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

## Support

https://solace.com/support

## Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:
- [Spring Docs - Spring Boot Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](//github.com/snicoll-demos/spring-boot-master-auto-configuration)



For more information about Solace technology for Spring Cloud please visit these resources:
- [Solace Spring Cloud](//github.com/SolaceProducts/solace-spring-cloud)

For more information about Solace technology in general please visit these resources:

- The [Solace Developer Portal](//dev.solace.com)
- Understanding [Solace technology](http://dev.solace.com/tech/)
- Ask the [Solace community](http://dev.solace.com/community/)

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
