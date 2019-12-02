# Solace Java CFEnv

## Overview

This project loads Cloud Foundry (CF) environment information to autodetect local Solace PubSub+ services.

## Table of Contents

* [Getting Started](#getting-started)
    * [Getting Started - Maven](#getting-started---maven)
    * [Getting Started - Gradle 4](#getting-started---gradle-4)
    * [Getting Started - Gradle 5](#getting-started---gradle-5)
* [Usage](#usage)
* [Functionality](#functionality)

---

## Getting Started

If you depend on `solace-spring-boot-starter`, `solace-java-spring-boot-starter` or `solace-jms-spring-boot-starter`, this dependency will be included transitively by default.

If, however, you want ONLY the `solace-java-cfenv` artifact, you can declare the dependency by itself. You will need to first import `solace-spring-boot-bom` as a BOM dependency. In Gradle 4 or earlier, this requires use of the `dependency-management-plugin`.

### Getting Started - Maven
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
            <artifactId>solace-java-cfenv</artifactId>    
        </dependency>
    </dependencies>
```

### Getting Started - Gradle 4
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
        compile("com.solace.spring.boot:solace-java-cfenv")
    }
```

### Getting Started - Gradle 5
```groovy
    /* Add me to your build.gradle */
    buildscript {
        ext {
            springBootVersion = '2.2.0.RELEASE'
        }
    }
    
    dependencies {
        implementation(platform("com.solace.spring.boot:solace-spring-boot-bom:${springBootVersion}"))
        implementation("com.solace.spring.boot:solace-java-cfenv")
    }
```

## Usage

This will get the `SolaceServiceCredentials` for each Solace PubSub+ service that is available in the current Cloud Foundry environment.

```java
List<SolaceServiceCredentials> solaceServiceCredentialsList;
solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
```

Note: If no Solace PubSub+ services are found, the list will be empty (not null).

## Functionality

Any CF service with either the Solace label or tag (defined in [SolaceServiceCredentialsFactory.java](src/main/java/com/solace/spring/cloud/core/SolaceServiceCredentialsFactory.java)), or both the label and tag, is considered to be a Solace PubSub+ service. These services will be autodetected by Java CFEnv.
