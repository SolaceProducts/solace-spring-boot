package com.solace.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import com.solacesystems.jcsmp.SolaceSessionOAuth2TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;

class SolaceOAuthClientConfigurationTest {

  @SpringBootApplication
  public static class TestApp {

  }

  @Test
  void verifyApplicationContextContainsRequiredBeans() {
    try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
        .profiles("oauthConfigIT").sources(TestApp.class)
        .properties(String.format("%s=%s", "solace.java.api-properties.AUTHENTICATION_SCHEME",
            "AUTHENTICATION_SCHEME_OAUTH2"))
        .run()) {
      assertThat(context.isRunning()).isTrue();
      assertThat(context.getBean(SolaceSessionOAuth2TokenProvider.class)).isNotNull();
      assertThat(
          context.getBean(AuthorizedClientServiceOAuth2AuthorizedClientManager.class)).isNotNull();
    }
  }

  @Test
  void verifyApplicationContextContainsRequiredBeans2() {
    try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
        .profiles("oauthConfigIT").sources(TestApp.class)
        .properties(String.format("%s=%s", "solace.java.apiProperties.AUTHENTICATION_SCHEME",
            "AUTHENTICATION_SCHEME_OAUTH2"))
        .run()) {
      assertThat(context.isRunning()).isTrue();
      assertThat(context.getBean(SolaceSessionOAuth2TokenProvider.class)).isNotNull();
      assertThat(
          context.getBean(AuthorizedClientServiceOAuth2AuthorizedClientManager.class)).isNotNull();
    }
  }

  @Test
  void verifyApplicationContextDoesNotContainOAuth2BeansWhenAuthSchemeIsNotOAuth2() {
    try (ConfigurableApplicationContext context = new SpringApplicationBuilder()
        .profiles("oauthConfigIT").sources(TestApp.class)
        .properties(String.format("%s=%s", "solace.java.apiProperties.AUTHENTICATION_SCHEME",
            "AUTHENTICATION_SCHEME_BASIC"))
        .run()) {
      assertThat(context.isRunning()).isTrue();

      assertThatThrownBy(() -> context.getBean(SolaceSessionOAuth2TokenProvider.class))
          .isInstanceOf(NoSuchBeanDefinitionException.class);
      assertThatThrownBy(() -> context.getBean(AuthorizedClientServiceOAuth2AuthorizedClientManager.class))
          .isInstanceOf(NoSuchBeanDefinitionException.class);
    }
  }
}