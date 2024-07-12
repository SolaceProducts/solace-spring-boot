package com.solace.it.util.semp;

import org.osgi.annotation.versioning.ProviderType;

public class SempClientException extends RuntimeException {

  public SempClientException(String message) {
    super(message);
  }

  public SempClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public SempClientException(Throwable cause) {
    super(cause);
  }


  /**
   * A class for raising errors when client authentication fails.
   *
   * @since 1.0
   */
  @ProviderType
  public static class AuthenticationException extends SempClientException {

    private static final long serialVersionUID = -4840876322728337412L;

    /**
     * Creates an instance of {@code AuthenticationException} when client authentication fails with
     * an additional message.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @since 1.0
     */
    public AuthenticationException(String message) {
      super(message);
    }

    /**
     * Creates an instance of {@code AuthenticationException} when client authentication fails with
     * an additional message and a {@code Throwable}.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @param t       the cause that is saved for later retrieval by the {@link #getCause()} method.
     *                A  {@code null} value is permitted, and indicates that the cause is
     *                non-existent or unknown.
     * @since 1.0
     */
    public AuthenticationException(String message, Throwable t) {
      super(message, t);
    }
  }

  /**
   * A class for raising errors when client authorizations fails, client authorizations unsupported
   * or not enabled for the service
   *
   * @since 1.0
   */
  @ProviderType
  public static class AuthorizationException extends SempClientException {

    private static final long serialVersionUID = -2315053666285971354L;

    /**
     * Creates an instance of {@code AuthorizationException} when client authorizations fails with
     * an additional message.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @since 1.0
     */
    public AuthorizationException(String message) {
      super(message);
    }

    /**
     * Creates an instance of {@code AuthorizationException} when client authorizations fails with
     * an additional message and a {@code Throwable}.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @param t       the cause that is saved for later retrieval by the {@link #getCause()} method.
     *                A  {@code null} value is permitted, and indicates that the cause is
     *                non-existent or unknown.
     * @since 1.0
     */
    public AuthorizationException(String message, Throwable t) {
      super(message, t);
    }
  }

  /**
   * A class for raising errors when a remote resource like a queue, vpn is not found on a broker.
   *
   * @since 1.0
   */
  @ProviderType
  public static class MissingResourceException extends SempClientException {

    private static final long serialVersionUID = 3777381415318250678L;

    /**
     * Creates an instance of {@code MissingResourceException} with a detailed message of missing a
     * resource situation.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @since 1.0
     */
    public MissingResourceException(String message) {
      super(message);
    }

    /**
     * Creates an instance of {@code MissingResourceException} with a detailed message of missing a
     * * resource situation and a {@code Throwable}.
     *
     * @param message the detailed message. The detailed message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     * @param t       the cause that is saved for later retrieval by the {@link #getCause()} method.
     *                A  {@code null} value is permitted, and indicates that the cause is
     *                non-existent or unknown.
     * @since 1.0
     */
    public MissingResourceException(String message, Throwable t) {
      super(message, t);
    }

  }
}