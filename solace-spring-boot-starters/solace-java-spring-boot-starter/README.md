# Spring Boot Auto-Configuration for the Solace Java API

This project provides Spring Boot Auto-Configuration and an associated Spring Boot Starter for the Solace Java API. The goal of this project is to make it easy to auto wire the Solace Java API within your application so you can take advantage of all the benefits of Spring Boot auto-configuration.

## Contents

* [Overview](#overview)
* [Using Auto-Configuration in your App](#using-auto-configuration-in-your-app)
* [Using OAuth2 Authentication Scheme](#using-oauth2-authentication-scheme)
* [Resources](#resources)

---

## Overview

As stated this project provides a Spring Boot Auto-Configuration implementation and a Spring Boot Starter pom for the Solace Java API. The goal of this project is to make it easier to use the Solace Java API with Spring Boot auto-configuration through the `@Autowired` annotation.

The artifacts are published to Maven Central so it should be familiar and intuitive to use this project in your applications.

One item to note as described below is that this project introduces a new factory for Solace Java API sessions: `SpringJCSMPFactory`. Overtime the Solace Java API may introduce a similar factory and remove the need for this custom extension. For now however, this is included in the auto-configuration jar for ease of use.

## Using Auto-Configuration in your App

See the associated [`solace-java-sample-app`](../../solace-spring-boot-samples/solace-java-sample-app) for an example of how this is all put together in a simple application. You'll need to do three steps:

1. Update your build.
2. Autowire a `SpringJCSMPFactory`.
3. Configure the application to use a Solace PubSub+ service.

### Updating your build

The releases from this project are hosted in [Maven Central](//mvnrepository.com/artifact/com.solace.spring.boot/solace-java-spring-boot-starter).

The easiest way to get started is to include the `solace-java-spring-boot-starter` (or `solace-spring-boot-starter`) in your application. For an examples see the [Java Sample App](../../solace-spring-boot-samples/solace-java-sample-app) in this project.

Here is how to include the latest spring boot starter in your project using Gradle and Maven. You can also add a specific version from [Maven Central](//mvnrepository.com/artifact/com.solace.spring.boot/solace-java-spring-boot-starter ).
Note that you'll need to include version 3.1.0 or later to use Spring Boot release 2.x.

#### Using it with Gradle

```groovy
// Solace Java API & auto-configuration
compile("com.solace.spring.boot:solace-java-spring-boot-starter:5.2.0")
```

#### Using it with Maven

```xml
<!-- Solace Java API & auto-configuration-->
<dependency>
	<groupId>com.solace.spring.boot</groupId>
	<artifactId>solace-java-spring-boot-starter</artifactId>
	<version>5.2.0</version>
</dependency>
```

### Using Spring Dependency Auto-Configuration (@SpringBootApplication & @Autowired)

Now in your application code, you can simply declare the `SpringJCSMPFactory` and annotate it so that it is autowired:

```java
@Autowired
private SpringJCSMPFactory solaceFactory;
```

Once you have the `SpringJCSMPFactory`, it behaves just like the `JCSMPFactory` and can be used to create sessions. For example:

```java
final JCSMPSession session = solaceFactory.createSession();
```

The `SpringJCSMPFactory` is a wrapper of the singleton `JCSMPFactory` which contains an associated `JCSMPProperties`. This facilitates auto-wiring by Spring but otherwise maintains the familiar `JCSMPFactory` interface known to users of the Solace Java API.

Alternatively, you could autowire JCSMPProperties to create your own customized `SpringJCSMPFactory`:

```java
/* The properties of a JCSMP connection */
@Autowired
private JCSMPProperties jcsmpProperties;
```

### Configure the Application to use your Solace PubSub+ Service Credentials

The configuration of the `SpringJCSMPFactory` can be done through the [`application.properties` file](../../solace-spring-boot-samples/solace-java-sample-app/src/main/resources/application.properties). This is where users can control the Solace Java API properties. Currently this project supports direct configuration of the following properties:

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
solace.java.oauth2ClientRegistrationId ##Set it when OAuth2 authentication scheme enabled. Reference to the Spring OAuth2 client registration-id.
```

Where reasonable, sensible defaults are always chosen. So a developer using a Solace PubSub+ message broker and wishing to use the default message-vpn may only set the `solace.java.host`.

See [`SolaceJavaProperties`](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solace/spring/boot/autoconfigure/SolaceJavaProperties.java) for the most up to date list.

Any additional Solace Java API properties can be set through configuring `solace.java.apiProperties.<Property>` where `<Property>` is the name of the property as defined in the [Solace Java API documentation for `com.solacesystems.jcsmp.JCSMPProperties`](//docs.solace.com/API-Developer-Online-Ref-Documentation/java/constant-values.html#com.solacesystems.jcsmp.JCSMPProperties.ACK_EVENT_MODE), for example:

```
solace.java.apiProperties.reapply_subscriptions=false
solace.java.apiProperties.ssl_trust_store=/path/to/truststore
solace.java.apiProperties.client_channel_properties.keepAliveIntervalInMillis=3000
```

Note that the direct configuration of `solace.java.` properties takes precedence over the `solace.java.apiProperties.`.

## Using OAuth2 Authentication Scheme

This Spring Boot starter for Solace Java API supports OAuth2 authentication scheme. It requires a version of Solace PubSub+ broker that supports OAuth2 authentication scheme. 

The Solace PubSub+ Broker should be setup for OAuth2 authentication. Refer to
the [Solace PubSub+: Configuring-OAuth-Authorization](https://docs.solace.com/Security/Configuring-OAuth-Authorization.htm)
for more information.
See [Azure OAuth Setup](https://solace.com/blog/azure-oauth-setup-for-solace-rest-and-smf-clients/)
for example.

You may also like to check
the [OAuth2 Integration Test](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/test/java/com/solace/spring/boot/autoconfigure/springBootTests/MessagingWithOAuthIT.java)
for more information.

> [!NOTE]
> The OAuth profile on Solace PubSub+ broker should be setup for Resource Server role. This Solace
> Java API Starer OAuth2 authentication scheme supports ```client_credentials``` grant type out-of-the
> box.

> [!TIP]
> The OAuth2 grant type ```client_credentials``` is used for machine to machine authentication, it
> is recommended that Token expiry time is not too short as it may cause frequent token refreshes and
> impact the performance.

### Using OAuth2 Authentication Scheme with Solace Java API

To use OAuth2 authentication scheme with Solace Java API, follow these steps:

Firstly, add the required dependencies to your `build.gradle` file:

```groovy
compile("org.springframework.boot:spring-boot-starter-oauth2-client")
```

or `pom.xml` file:

```xml

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Secondly, add `@EnableWebSecurity` annotation to your Spring Boot application class:

```java

@SpringBootApplication
@EnableWebSecurity
public class DemoApplication {

}
```

Finally, configure the Spring OAuth2 Client Registration provider through following properties in
your `application.properties` file:

```
##spring.security.oauth2.client.registration.<registration-id>.provider=<provider-id>
spring.security.oauth2.client.registration.my-oauth2-client.provider=my-auth-server
spring.security.oauth2.client.registration.my-oauth2-client.client-id=replace-client-id-here
spring.security.oauth2.client.registration.my-oauth2-client.client-secret=replace-client-secret-here
spring.security.oauth2.client.registration.my-oauth2-client.authorization-grant-type=client_credentials  ## only client_credentials grant type is supported

##spring.security.oauth2.client.provider.<provider-id>.token-uri=<token-uri>
spring.security.oauth2.client.provider.my-auth-server.token-uri=replace-token-uri-here

solace.java.host=tcps://localhost:55443  ## OATUH2 authentication scheme requires a secure connection to the broker
solace.java.msgVpn=replace-msgVpn-here
solace.java.oauth2ClientRegistrationId=my-oauth2-client ## Refers to the Spring OAuth2 client registration-id defined above
solace.java.apiProperties.AUTHENTICATION_SCHEME=AUTHENTICATION_SCHEME_OAUTH2
```

See
the [Solace Java API OAuth2 Sample](../../solace-spring-boot-samples/solace-java-oauth2-sample-app)
for an example of how to use OAuth2 authentication scheme.

### Customizing OAuth2 Token Injection and Token Refresh

The Solace Java API OAuth2 authentication scheme supports customizing the OAuth2 token injection and
token refresh.

Create your custom implementation of
the [SolaceSessionOAuth2TokenProvider](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solacesystems/jcsmp/SolaceSessionOAuth2TokenProvider.java)
interface to injection initial token.
Refer [DefaultSolaceSessionOAuth2TokenProvider](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solacesystems/jcsmp/DefaultSolaceSessionOAuth2TokenProvider.java)
for sample implementation.

Similarly, create your custom implementation of
the [SolaceOAuth2SessionEventHandler](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solacesystems/jcsmp/SolaceOAuth2SessionEventHandler.java)
interface to refresh token.
Refer [DefaultSolaceOAuth2SessionEventHandler](../../solace-spring-boot-autoconfigure/solace-java-spring-boot-autoconfigure/src/main/java/com/solacesystems/jcsmp/DefaultSolaceOAuth2SessionEventHandler.java)
for sample implementation.


## Resources

For more information about Spring Boot Auto-Configuration and Starters try these resources:

- [Spring Docs - Spring Boot Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#using-boot-auto-configuration)
- [Spring Docs - Developing Auto-Configuration](//docs.spring.io/autorepo/docs/spring-boot/current/reference/htmlsingle/#boot-features-developing-auto-configuration)
- [GitHub Tutorial - Master Spring Boot Auto-Configuration](//github.com/snicoll-demos/spring-boot-master-auto-configuration)

For more information about Solace technology in general please visit these resources:

- The [Solace Developer Portal](//dev.solace.com)
- Understanding [Solace technology.](//dev.solace.com/tech/)
- Ask the [Solace community](//dev.solace.com/community/).
