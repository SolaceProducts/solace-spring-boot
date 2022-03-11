# Solace Java CFEnv

## Overview

This project loads Cloud Foundry (CF) environment information to autodetect local Solace PubSub+ services.

## Table of Contents

* [Getting Started](#getting-started)
    * [Getting Started - Maven](#getting-started---maven)
    * [Getting Started - Gradle](#getting-started---gradle)
* [Usage](#usage)
* [Functionality](#functionality)

---

## Getting Started

If you depend on `solace-spring-boot-starter`, `solace-java-spring-boot-starter` or `solace-jms-spring-boot-starter`, this dependency will be included transitively by default.

If, however, you want ONLY the `solace-java-cfenv` artifact, you can declare the dependency by itself.

### Getting Started - Maven
```xml
<!-- Solace Java CFEnv -->
<dependency>
    <groupId>com.solace.cloud.cloudfoundry</groupId>
    <artifactId>solace-java-cfenv</artifactId>
    <version>1.2.1</version>
</dependency>
```

### Getting Started - Gradle
```groovy
/* Solace Java CFEnv */
compile("com.solace.cloud.cloudfoundry:solace-java-cfenv:1.2.1")
```

## Usage

This will get the `SolaceServiceCredentials` for each Solace PubSub+ service that is available in the current Cloud Foundry environment.

```java
List<SolaceServiceCredentials> solaceServiceCredentialsList;
solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
```

Note: If no Solace PubSub+ services are found, the list will be empty (not null).

## Functionality

Any CF service with either the Solace label or tag (defined in [SolaceServiceCredentialsFactory.java](./src/main/java/com/solace/spring/cloud/core/SolaceServiceCredentialsFactory.java)), or both the label and tag, is considered to be a Solace PubSub+ service. These services will be auto-detected by Java CFEnv.

## Resources

For more information about Pivotal Java CFEnv try these resources:
- [Github Source - Java CFEnv](//github.com/pivotal-cf/java-cfenv)