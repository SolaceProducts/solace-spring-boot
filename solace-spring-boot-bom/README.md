# Solace Spring Boot Bill of Materials (BOM)

## Contents

* [Overview](#overview)
* [Spring Boot Version Compatibility](#spring-boot-version-compatibility)
* [Including the BOM](#including-the-bom)

## Overview

The Solace Spring Boot Bill of Materials (BOM) is a POM file which defines the versions of Solace Spring Boot projects that are compatible to a particular version of Spring Boot.

## Spring Boot Version Compatibility

Consult the table below to determine which version of the BOM you need to use:

| Spring Boot | Solace Spring Boot BOM |
|-------------|------------------------|
| 2.2.4       | 1.0.0                  |
| 2.3.0       | 1.1.0                  |
| 2.6.4       | 1.2.x                  |
| 2.7.7       | 1.3.0                  |
| 3.0.6       | 2.0.0                  |
| 3.3.1       | 2.1.x                  |
| 3.3.3       | 2.2.0                  |
| 3.4.4       | 2.4.0                  |
| 3.5.5       | 2.5.0                  |
| 4.0.3       | 3.0.0                  |

## Including the BOM

The Solace Spring Boot BOM allows you to manage Solace Spring Boot dependency versions. You will use **one** of the following starters based on your messaging API preference:
- `solace-java-spring-boot-starter` - for using the Solace JCSMP API
- `solace-jms-spring-boot-starter` - for using the Solace JMS API

### Using it with Maven

#### For Solace JCSMP API

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.solace.spring.boot</groupId>
      <artifactId>solace-spring-boot-bom</artifactId>
      <version>3.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
<dependency>
  <groupId>com.solace.spring.boot</groupId>
  <artifactId>solace-java-spring-boot-starter</artifactId>
</dependency>
</dependencies>
```

#### For Solace JMS API

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.solace.spring.boot</groupId>
            <artifactId>solace-spring-boot-bom</artifactId>
            <version>3.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.solace.spring.boot</groupId>
        <artifactId>solace-jms-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### Using it with Gradle

#### For Solace JCSMP API

```groovy
dependencies {
    implementation(platform("com.solace.spring.boot:solace-spring-boot-bom:3.0.0"))
    implementation("com.solace.spring.boot:solace-java-spring-boot-starter")
}
```

#### For Solace JMS API

```groovy
dependencies {
    implementation(platform("com.solace.spring.boot:solace-spring-boot-bom:3.0.0"))
    implementation("com.solace.spring.boot:solace-jms-spring-boot-starter")
}
```