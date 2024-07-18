package com.solacesystems.jcsmp;

/**
 * The JCSMP {@link SessionEventHandler} when OAuth2 authentication scheme is being used.
 * Implementing classes should handle the OAuth2 token refresh logic when the session is
 * reconnecting. Refer {@link DefaultSolaceOAuth2SessionEventHandler} for the default
 * implementation.
 */
public interface SolaceOAuth2SessionEventHandler extends SessionEventHandler {

  /**
   * Sets the JCSMP session associated with this event handler.
   *
   * @param jcsmpSession The JCSMP session associated with this event handler.
   */
  void setJcsmpSession(JCSMPSession jcsmpSession);
}