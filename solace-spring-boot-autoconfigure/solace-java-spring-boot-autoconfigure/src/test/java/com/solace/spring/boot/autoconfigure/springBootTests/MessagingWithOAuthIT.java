package com.solace.spring.boot.autoconfigure.springBootTests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import com.solace.it.util.semp.config.BrokerConfiguratorBuilder;
import com.solace.it.util.semp.config.BrokerConfiguratorBuilder.BrokerConfigurator;
import com.solace.it.util.semp.monitor.BrokerMonitorBuilder;
import com.solace.it.util.semp.monitor.BrokerMonitorBuilder.BrokerMonitor;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solace.test.integration.semp.v2.action.ApiException;
import com.solace.test.integration.semp.v2.action.model.ActionMsgVpnClientDisconnect;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthenticationOauthProfile;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthenticationOauthProfile.OauthRoleEnum;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthorizationGroup;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClient;
import com.solacesystems.jcsmp.DefaultSolaceOAuth2SessionEventHandler;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SessionEventArgs;
import com.solacesystems.jcsmp.SessionEventHandler;
import com.solacesystems.jcsmp.SolaceSessionOAuth2TokenProvider;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    classes = SampleApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc()
@ActiveProfiles("oauthIT")
class MessagingWithOAuthIT implements
    MessagingServiceFreeTierBrokerTestContainerWithTlsAndOAuthSetup {

  private static final Logger logger = LoggerFactory.getLogger(MessagingWithOAuthIT.class);
  private static BrokerConfigurator solaceConfigUtil;
  private static BrokerMonitor solaceMonitorUtil;
  private static SempV2Api sempV2Api;

  final static String OAUTH_PROFILE_NAME = "SolaceOauthResourceServer";
  final static String AUTHORIZATION_GROUP_NAME = "solclient_oauth_auth_group";
  final static String MSG_VPN_DEFAULT = "default";

  @Autowired
  SpringJCSMPFactory springJCSMPFactory;

  @Autowired
  JCSMPProperties jcsmpProperties;

  @Autowired
  SolaceSessionOAuth2TokenProvider solaceSessionOAuth2TokenProvider;

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    String solaceHost = COMPOSE_CONTAINER.getServiceHost(PUBSUB_BROKER_SERVICE_NAME, 55443);
    int solaceSecureSMFPort = COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 55443);
    COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 55443);
    registry.add("solace.java.host",
        () -> String.format("tcps://%s:%s", solaceHost, solaceSecureSMFPort));

    String nginxHost = COMPOSE_CONTAINER.getServiceHost(NGINX_RPROXY_SERVICE_NAME, 10443);
    int nginxSecurePort = COMPOSE_CONTAINER.getServicePort(NGINX_RPROXY_SERVICE_NAME, 10443);
    registry.add("spring.security.oauth2.client.provider.my-auth-server.token-uri",
        () -> String.format(
            "https://%s:%s/auth/realms/solace-oauth-resource-server-role/protocol/openid-connect/token",
            nginxHost, nginxSecurePort));
  }

  @BeforeAll
  static void setUp() {
    try {
      String solaceHost = COMPOSE_CONTAINER.getServiceHost(PUBSUB_BROKER_SERVICE_NAME, 8080);
      int solaceSempPort = COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 8080);
      String sempUrl = String.format("http://%s:%s", solaceHost, solaceSempPort);
      sempV2Api = new SempV2Api(sempUrl, "admin", "admin");
      solaceConfigUtil = BrokerConfiguratorBuilder.create(sempV2Api).build();
      solaceMonitorUtil = BrokerMonitorBuilder.create(sempV2Api).build();

      logger.debug("Prepare to upload CA cert to the broker");
      final URL resource = MessagingWithOAuthIT.class.getClassLoader()
          .getResource("certs/rootCA/rootCA.pem");
      if (resource != null) {
        final File caFile = new File(resource.toURI());
        final String ca = Files.contentOf(caFile, StandardCharsets.US_ASCII);
        solaceConfigUtil.certAuthorities().setupCertAuthority("myCA", ca);
        logger.debug("CA cert is uploaded to the broker");
      } else {
        logger.error("CA cert file can't be uploaded");
        fail("Root certificate file can't be found");
      }

      //Setup Solace PubSub+ for OAuth2
      setupOAuth(MSG_VPN_DEFAULT);
    } catch (URISyntaxException e) {
      fail(e);
    }
  }

  private static void deleteOAuthSetup(String msgVpnName) {
    solaceConfigUtil.vpns().disableOAuthAuth(msgVpnName);
    solaceConfigUtil.vpns().deleteOAuthProfile(msgVpnName, OAUTH_PROFILE_NAME);
    solaceConfigUtil.vpns().deleteAuthorizationGroup(msgVpnName, AUTHORIZATION_GROUP_NAME);
  }

  private static void setupOAuth(String msgVpnName) {
    solaceConfigUtil.vpns().enableOAuthAuth(msgVpnName);
    solaceConfigUtil.vpns().createOAuthProfile(msgVpnName, oAuthProfileResourceServer());
    solaceConfigUtil.vpns().createAuthorizationGroup(msgVpnName, authorizationGroup1());
  }

  private static ConfigMsgVpnAuthenticationOauthProfile oAuthProfileResourceServer() {
    final String AUTHORIZATION_GROUP_CLAIM_NAME = "";
    final String ENDPOINT_JWKS = "https://solaceoauth:10443/auth/realms/solace-oauth-resource-server-role/protocol/openid-connect/certs";
    final String ENDPOINT_USERINFO = "https://solaceoauth:10443/auth/realms/solace-oauth-resource-server-role/protocol/openid-connect/userinfo";
    final String REALM2_ISSUER_IDENTIFIER = "https://solaceoauth:10443/auth/realms/solace-oauth-resource-server-role";

    return new ConfigMsgVpnAuthenticationOauthProfile()
        .enabled(true)
        .oauthProfileName(OAUTH_PROFILE_NAME)
        .authorizationGroupsClaimName(AUTHORIZATION_GROUP_CLAIM_NAME)
        .issuer(REALM2_ISSUER_IDENTIFIER)
        .endpointJwks(ENDPOINT_JWKS)
        .endpointUserinfo(ENDPOINT_USERINFO)
        .resourceServerParseAccessTokenEnabled(true)
        .resourceServerRequiredAudience("")
        .resourceServerRequiredIssuer("")
        .resourceServerRequiredScope("")
        .resourceServerValidateAudienceEnabled(false)
        .resourceServerValidateIssuerEnabled(false)
        .resourceServerValidateScopeEnabled(false)
        .resourceServerValidateTypeEnabled(false)
        .oauthRole(OauthRoleEnum.RESOURCE_SERVER);
  }

  private static ConfigMsgVpnAuthorizationGroup authorizationGroup1() {
    return new ConfigMsgVpnAuthorizationGroup()
        .authorizationGroupName(AUTHORIZATION_GROUP_NAME)
        .enabled(true)
        .aclProfileName("default")
        .clientProfileName("default");
  }

  private boolean isOAuth2() {
    return JCSMPProperties.AUTHENTICATION_SCHEME_OAUTH2.equalsIgnoreCase(
        jcsmpProperties.getStringProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
  }

  @Test
  void canConnectWhenAuthenticationSchemeIsOAuth2() {
    if (!isOAuth2()) {
      fail("Was expecting the authentication scheme to be OAuth2");
    }

    try {
      JCSMPSession jcsmpSession = springJCSMPFactory.createSession();
      jcsmpSession.connect();
      logger.info("Session connected successfully.");
      jcsmpSession.closeSession();
    } catch (Exception e) {
      fail(e);
    }
  }


  @Test
  @Tag("SLOW")
  void canRefreshTokenWhenAuthenticationSchemeIsOAuth2() {
    if (!isOAuth2()) {
      fail("Was expecting the authentication scheme to be OAuth2");
    }

    try {
      CountDownLatch refreshedTokenLatch = new CountDownLatch(1);
      SessionEventHandler sessionEventHandler = new DefaultSolaceOAuth2SessionEventHandler(
          jcsmpProperties, solaceSessionOAuth2TokenProvider) {
        @Override
        public void handleEvent(SessionEventArgs sessionEventArgs) {
          super.handleEvent(sessionEventArgs);
          logger.info("Token refreshed successfully.");
          refreshedTokenLatch.countDown();
        }
      };

      JCSMPSession jcsmpSession = springJCSMPFactory.createSession(
          springJCSMPFactory.getDefaultContext(), sessionEventHandler);
      jcsmpSession.connect();
      logger.info("Session connected successfully.");
      logger.info("Wait for session reconnect, to refresh token. Will take about 1 minute.");
      boolean success = refreshedTokenLatch.await(3, TimeUnit.MINUTES);
      if (!success) {
        fail("Timed out waiting for token refresh");
      }
      jcsmpSession.closeSession();
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  @Tag("SLOW")
  void canRefreshTokenWhenForceReconnect() {
    if (!isOAuth2()) {
      fail("Was expecting the authentication scheme to be OAuth2");
    }

    try {
      int numberOfReconnects = 10;
      CountDownLatch refreshedTokenLatch = new CountDownLatch(numberOfReconnects);
      SessionEventHandler sessionEventHandler = new DefaultSolaceOAuth2SessionEventHandler(
          jcsmpProperties, solaceSessionOAuth2TokenProvider) {
        @Override
        public void handleEvent(SessionEventArgs sessionEventArgs) {
          super.handleEvent(sessionEventArgs);
          logger.info("Token refreshed successfully.");
          refreshedTokenLatch.countDown();
        }
      };

      JCSMPSession jcsmpSession = springJCSMPFactory.createSession(
          springJCSMPFactory.getDefaultContext(), sessionEventHandler);
      jcsmpSession.connect();
      logger.info("Session connected successfully.");

      AtomicBoolean failed = new AtomicBoolean(false);
      Thread t = new Thread(() -> {
        for (int i = 0; i < numberOfReconnects; i++) {
          try {
            Thread.sleep(10_000);
            MonitorMsgVpnClient msgVpnClient = solaceMonitorUtil.vpnClients()
                .queryVpnClients(MSG_VPN_DEFAULT)
                .stream()
                .filter(client -> client.getClientUsername().startsWith("default"))
                .findFirst().orElse(null);

            if (msgVpnClient == null) {
              throw new RuntimeException("Client not found");
            }

            try {
              logger.info("Forcing Session Reconnect for client: {}", msgVpnClient.getClientName());
              sempV2Api.action()
                  .doMsgVpnClientDisconnect(MSG_VPN_DEFAULT, msgVpnClient.getClientName(),
                      new ActionMsgVpnClientDisconnect());
            } catch (ApiException e) {
              throw new RuntimeException(e);
            }
          } catch (Exception e) {
            failed.set(true);
            return;
          }
        }
      });

      t.start();

      logger.info("Wait for session reconnect, to refresh token");
      boolean success = refreshedTokenLatch.await(3, TimeUnit.MINUTES);
      if (!success) {
        fail("Timed out waiting for token refresh");
      }

      jcsmpSession.closeSession();

      assertFalse(failed.get(), "Failed to force reconnect");
    } catch (Exception e) {
      fail(e);
    }
  }
}