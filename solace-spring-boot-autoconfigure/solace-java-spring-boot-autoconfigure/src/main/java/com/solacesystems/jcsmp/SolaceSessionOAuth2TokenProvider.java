package com.solacesystems.jcsmp;

/**
 * Interface for providing OAuth2 access tokens for Solace sessions. Implementing classes should
 * provide a method to fetch and return the current OAuth2 access token. Refer
 * {@link DefaultSolaceSessionOAuth2TokenProvider} for a default implementation.
 */
public interface SolaceSessionOAuth2TokenProvider {

  /**
   * Fetches and returns the current OAuth2 access token.
   *
   * @return The current OAuth2 access token.
   */
  String getAccessToken();
}