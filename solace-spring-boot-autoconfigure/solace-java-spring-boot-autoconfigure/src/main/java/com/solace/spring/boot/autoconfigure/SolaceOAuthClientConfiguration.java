package com.solace.spring.boot.autoconfigure;

import com.solacesystems.jcsmp.DefaultSolaceSessionOAuth2TokenProvider;
import com.solacesystems.jcsmp.JCSMPProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Configuration class for Solace OAuth client. This configuration is only active when the
 * 'solace.java.apiProperties.AUTHENTICATION_SCHEME' property is set to
 * 'AUTHENTICATION_SCHEME_OAUTH2'.
 */
@Configuration
@ConditionalOnProperty(prefix = "solace.java.apiProperties", name = "AUTHENTICATION_SCHEME",
    havingValue = "AUTHENTICATION_SCHEME_OAUTH2")
@Import(OAuth2ClientAutoConfiguration.class)
public class SolaceOAuthClientConfiguration {

  /**
   * Creates and configures an OAuth2AuthorizedClientManager for Solace session. This manager is
   * configured with OAuth2AuthorizedClientProvider for client credentials and refresh token.
   *
   * @param clientRegistrationRepository  Repository of client registrations.
   * @param oAuth2AuthorizedClientService Service for authorized OAuth2 clients.
   * @return Configured OAuth2AuthorizedClientManager.
   */
  @Bean
  public AuthorizedClientServiceOAuth2AuthorizedClientManager solaceOAuthAuthorizedClientServiceAndManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
    final OAuth2AuthorizedClientProvider clientCredentialsAuthorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .clientCredentials()
            .refreshToken()
            .build();

    final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository, oAuth2AuthorizedClientService);
    authorizedClientManager.setAuthorizedClientProvider(clientCredentialsAuthorizedClientProvider);

    return authorizedClientManager;
  }

  /**
   * Creates a SolaceSessionOAuth2TokenProvider for providing OAuth2 access tokens for Solace
   * sessions.
   *
   * @param jcsmpProperties                              The JCSMP properties.
   * @param solaceOAuthAuthorizedClientServiceAndManager The OAuth2AuthorizedClientManager for
   *                                                     Solace session.
   * @return Configured SolaceSessionOAuth2TokenProvider.
   */
  @Bean
  public DefaultSolaceSessionOAuth2TokenProvider solaceSessionOAuth2TokenProvider(
      JCSMPProperties jcsmpProperties,
      AuthorizedClientServiceOAuth2AuthorizedClientManager solaceOAuthAuthorizedClientServiceAndManager) {
    return new DefaultSolaceSessionOAuth2TokenProvider(jcsmpProperties,
        solaceOAuthAuthorizedClientServiceAndManager);
  }
}