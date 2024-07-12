package com.solacesystems.jcsmp;

import static com.solacesystems.jcsmp.JCSMPProperties.AUTHENTICATION_SCHEME;
import static com.solacesystems.jcsmp.JCSMPProperties.AUTHENTICATION_SCHEME_OAUTH2;
import static com.solacesystems.jcsmp.SessionEvent.RECONNECTING;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of SolaceOAuth2SessionEventHandler. This class handles the OAuth2 token
 * refresh logic when the session is reconnecting.
 */
public class DefaultSolaceOAuth2SessionEventHandler implements SolaceOAuth2SessionEventHandler {

  private static final Log logger = LogFactory.getLog(DefaultSolaceOAuth2SessionEventHandler.class);

  protected final SolaceSessionOAuth2TokenProvider solaceSessionOAuth2TokenProvider;
  protected final JCSMPProperties jcsmpProperties;
  protected JCSMPSession jcsmpSession;

  /**
   * Constructs a new DefaultSolaceOAuth2SessionEventHandler with the provided JCSMP properties and
   * OAuth2 token provider.
   *
   * @param jcsmpProperties                  The JCSMP properties.
   * @param solaceSessionOAuth2TokenProvider The OAuth2 token provider.
   */
  public DefaultSolaceOAuth2SessionEventHandler(JCSMPProperties jcsmpProperties,
      SolaceSessionOAuth2TokenProvider solaceSessionOAuth2TokenProvider) {
    this.jcsmpProperties = jcsmpProperties;
    this.solaceSessionOAuth2TokenProvider = solaceSessionOAuth2TokenProvider;

    Objects.requireNonNull(jcsmpProperties);
    if (isAuthSchemeOAuth2()) {
      Objects.requireNonNull(solaceSessionOAuth2TokenProvider);
    }
  }

  @Override
  public void handleEvent(SessionEventArgs sessionEventArgs) {
    final SessionEvent event = sessionEventArgs.getEvent();
    if (event == RECONNECTING && isAuthSchemeOAuth2()) {
      refreshOAuth2AccessToken();
    }
  }

  protected boolean isAuthSchemeOAuth2() {
    return AUTHENTICATION_SCHEME_OAUTH2.equalsIgnoreCase(
        jcsmpProperties.getStringProperty(AUTHENTICATION_SCHEME));
  }

  private void refreshOAuth2AccessToken() {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Refreshing OAuth2 access token");
      }

      final String newAccessToken = solaceSessionOAuth2TokenProvider.getAccessToken();
      this.jcsmpSession.setProperty(JCSMPProperties.OAUTH2_ACCESS_TOKEN, newAccessToken);
    } catch (JCSMPException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception while fetching/providing refreshed access token: " + e);
      }
    }
  }

  @Override
  public void setJcsmpSession(JCSMPSession jcsmpSession) {
    this.jcsmpSession = jcsmpSession;
  }
}