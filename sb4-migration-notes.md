# Spring Boot 4 Migration Notes

Spring Boot 3.5.5 → 4.0.3 — all 12 unit tests passing.

## Changes Applied

| File | Change |
|------|--------|
| `pom.xml` (root) | `spring.boot.version`: `3.5.5` → `4.0.3` |
| `solace-spring-boot-parent/pom.xml` | Surefire/Failsafe: `2.22.2` → `3.5.4` |
| `solace-java-spring-boot-autoconfigure/pom.xml` | Added `spring-boot-jms` (optional) + `spring-boot-webmvc-test` (test) |
| `solace-jms-spring-boot-autoconfigure/pom.xml` | Added `spring-boot-jms` dependency |
| `SolaceJavaAutoConfiguration.java` | Import: `JmsAutoConfiguration` → new `jms.autoconfigure` package |
| `SolaceJmsAutoConfiguration.java` | Import: `JmsAutoConfiguration` → new `jms.autoconfigure` package |
| `SolaceOAuthClientConfiguration.java` | Import: `OAuth2ClientAutoConfiguration` → removed `servlet` sub-package |
| `SolaceJmsAutoConfigurationTest.java` | Import: `JmsAutoConfiguration` → new package |
| `MessagingWithClientCertAuthIT.java` | Import: `AutoConfigureMockMvc` → new `webmvc.test.autoconfigure` package |
| `MessagingWithOAuthIT.java` | Import: `AutoConfigureMockMvc` → new `webmvc.test.autoconfigure` package |

## Package Relocations

### JmsAutoConfiguration
- **Old:** `org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration`
- **New:** `org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration`
- **Module:** Add `org.springframework.boot:spring-boot-jms` as dependency (optional for libraries using `@AutoConfigureBefore`)

### OAuth2ClientAutoConfiguration
- **Old:** `org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration`
- **New:** `org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration`
- The `servlet` sub-package was dropped. Available transitively via `spring-boot-starter-oauth2-client`.

### AutoConfigureMockMvc
- **Old:** `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`
- **New:** `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
- **Module:** Add `org.springframework.boot:spring-boot-webmvc-test` as test dependency

## Build Plugin Update

Maven Surefire/Failsafe `2.22.2` is incompatible with JUnit Platform 1.11+ (used in Spring Boot 4).
The old version fails silently — tests show 0 runs but BUILD SUCCESS, masking real failures.
Update both plugins to `3.5.4`.

## Rollback

```bash
git reset --hard pre-spring-boot-4-migration
```
