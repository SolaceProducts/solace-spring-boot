package com.solacesystems.jcsmp;

import static com.solacesystems.jcsmp.JCSMPProperties.USERNAME;
import com.solace.spring.boot.autoconfigure.SolaceJavaProperties;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * Default implementation of SolaceSessionOAuth2TokenProvider. This class fetches and returns the
 * current OAuth2 access token using the provided JCSMP properties and OAuth2 authorized client
 * manager.
 */
public class DefaultSolaceSessionOAuth2TokenProvider implements SolaceSessionOAuth2TokenProvider {

  private static final Log logger = LogFactory.getLog(
      DefaultSolaceSessionOAuth2TokenProvider.class);

  private final JCSMPProperties jcsmpProperties;
  private final AuthorizedClientServiceOAuth2AuthorizedClientManager solaceOAuthAuthorizedClientServiceAndManager;

  /**
   * Constructs a new DefaultSolaceSessionOAuth2TokenProvider with the provided JCSMP properties and
   * OAuth2 authorized client manager.
   *
   * @param jcsmpProperties                              The JCSMP properties.
   * @param solaceOAuthAuthorizedClientServiceAndManager The OAuth2 authorized client manager.
   */
  public DefaultSolaceSessionOAuth2TokenProvider(JCSMPProperties jcsmpProperties,
      AuthorizedClientServiceOAuth2AuthorizedClientManager solaceOAuthAuthorizedClientServiceAndManager) {
    Objects.requireNonNull(jcsmpProperties);
    Objects.requireNonNull(solaceOAuthAuthorizedClientServiceAndManager);
    this.jcsmpProperties = jcsmpProperties;
    this.solaceOAuthAuthorizedClientServiceAndManager = solaceOAuthAuthorizedClientServiceAndManager;
  }

  @Override
  public String getAccessToken() {
    try {
      final String clientUserName = Objects.toString(
          jcsmpProperties.getStringProperty(USERNAME), "spring-default-client-username");
      final String oauth2ClientRegistrationId = jcsmpProperties
          .getStringProperty(SolaceJavaProperties.SPRING_OAUTH2_CLIENT_REGISTRATION_ID);

      if (logger.isInfoEnabled()) {
        logger.info(String.format("Fetching OAuth2 access token using client registration ID: %s",
            oauth2ClientRegistrationId));
      }

      final OAuth2AuthorizeRequest authorizeRequest =
          OAuth2AuthorizeRequest.withClientRegistrationId(oauth2ClientRegistrationId)
              .principal(clientUserName)
              .build();

      //Perform the actual authorization request using the authorized client service and authorized
      //client manager. This is where the JWT is retrieved from the OAuth/OIDC servers.
      final OAuth2AuthorizedClient oAuth2AuthorizedClient =
          solaceOAuthAuthorizedClientServiceAndManager.authorize(authorizeRequest);

      //Get the token from the authorized client object
      final OAuth2AccessToken accessToken = Objects.requireNonNull(oAuth2AuthorizedClient)
          .getAccessToken();

      return accessToken.getTokenValue();
    } catch (Throwable t) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception while fetching OAuth2 access token: " + t);
      }
      throw t;
    }
  }
}