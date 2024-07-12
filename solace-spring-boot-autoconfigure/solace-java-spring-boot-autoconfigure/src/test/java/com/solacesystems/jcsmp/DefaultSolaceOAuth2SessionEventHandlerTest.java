package com.solacesystems.jcsmp;

import static com.solacesystems.jcsmp.JCSMPProperties.OAUTH2_ACCESS_TOKEN;
import static com.solacesystems.jcsmp.SessionEvent.DOWN_ERROR;
import static com.solacesystems.jcsmp.SessionEvent.RECONNECTING;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DefaultSolaceOAuth2SessionEventHandlerTest {

  private SolaceSessionOAuth2TokenProvider mockTokenProvider;
  private JCSMPSession mockSession;

  private DefaultSolaceOAuth2SessionEventHandler eventHandler;

  @BeforeEach
  void setUp() {
    mockTokenProvider = Mockito.mock(SolaceSessionOAuth2TokenProvider.class);
    mockSession = Mockito.mock(JCSMPSession.class);
    JCSMPProperties jcsmpProperties = new JCSMPProperties();
    jcsmpProperties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME,
        JCSMPProperties.AUTHENTICATION_SCHEME_OAUTH2);
    eventHandler = new DefaultSolaceOAuth2SessionEventHandler(jcsmpProperties, mockTokenProvider);
    eventHandler.setJcsmpSession(mockSession);
  }

  @Test
  void shouldRefreshTokenOnReconnectingEvent() throws JCSMPException {
    when(mockTokenProvider.getAccessToken()).thenReturn("newAccessToken");
    SessionEventArgs reconnecting = new SessionEventArgs(RECONNECTING, "Reconnecting", null, 0);

    eventHandler.handleEvent(reconnecting);

    verify(mockTokenProvider, times(1)).getAccessToken();
    verify(mockSession, times(1)).setProperty(OAUTH2_ACCESS_TOKEN, "newAccessToken");
  }

  @Test
  void shouldNotRefreshTokenOnNonReconnectingEvent() throws JCSMPException {
    SessionEventArgs downError = new SessionEventArgs(DOWN_ERROR, "DownError", null, 0);

    eventHandler.handleEvent(downError);

    verify(mockTokenProvider, never()).getAccessToken();
    verify(mockSession, never()).setProperty(eq(OAUTH2_ACCESS_TOKEN), anyString());
  }

  @Test
  void shouldHandleExceptionWhenRefreshingToken() throws JCSMPException {
    doThrow(new JCSMPException("Test exception")).when(mockSession)
        .setProperty(eq(OAUTH2_ACCESS_TOKEN), anyString());
    SessionEventArgs reconnecting = new SessionEventArgs(RECONNECTING, "Reconnecting", null, 0);

    eventHandler.handleEvent(reconnecting);

    verify(mockTokenProvider, times(1)).getAccessToken();
    // No need to verify logging, just ensure no exception is thrown to the caller
  }
}