package com.solace.spring.boot.autoconfigure.springBootTests;

import static org.junit.jupiter.api.Assertions.fail;
import com.solace.it.util.semp.config.BrokerConfiguratorBuilder;
import com.solace.it.util.semp.config.BrokerConfiguratorBuilder.BrokerConfigurator;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeAll;
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
@ActiveProfiles("clientCertAuthIT")
class MessagingWithClientCertAuthIT implements
    MessagingServiceFreeTierBrokerTestContainerWithTlsSetup {

  private static final Logger logger = LoggerFactory.getLogger(MessagingWithClientCertAuthIT.class);
  private static BrokerConfigurator solaceConfigUtil;
  final static String MSG_VPN_DEFAULT = "default";

  @Autowired
  SpringJCSMPFactory springJCSMPFactory;

  @Autowired
  JCSMPProperties jcsmpProperties;

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    String solaceHost = COMPOSE_CONTAINER.getServiceHost(PUBSUB_BROKER_SERVICE_NAME, 55443);
    int solaceSecureSMFPort = COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 55443);
    COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 55443);
    registry.add("solace.java.host",
        () -> String.format("tcps://%s:%s", solaceHost, solaceSecureSMFPort));

    registry.add("solace.java.apiProperties.SSL_TRUST_STORE",
        () -> new File("src/test/resources/certs/client/client-truststore.p12").getAbsolutePath());
    registry.add("solace.java.apiProperties.SSL_KEY_STORE",
        () -> new File("src/test/resources/certs/client/client-keystore.jks").getAbsolutePath());
  }

  @BeforeAll
  static void setUp() {
    try {
      String solaceHost = COMPOSE_CONTAINER.getServiceHost(PUBSUB_BROKER_SERVICE_NAME, 8080);
      int solaceSempPort = COMPOSE_CONTAINER.getServicePort(PUBSUB_BROKER_SERVICE_NAME, 8080);
      String sempUrl = String.format("http://%s:%s", solaceHost, solaceSempPort);
      SempV2Api sempV2Api = new SempV2Api(sempUrl, "admin", "admin");
      solaceConfigUtil = BrokerConfiguratorBuilder.create(sempV2Api).build();

      logger.debug("Prepare to upload CA cert to the broker");
      final URL resource = MessagingWithClientCertAuthIT.class.getClassLoader()
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

      //Enable client certificate authentication on the Solace PubSub+ Broker
      solaceConfigUtil.vpns().enableClientCertAuth(MSG_VPN_DEFAULT);
    } catch (URISyntaxException e) {
      fail(e);
    }
  }

  private boolean isClientCertificateAuthentication() {
    return JCSMPProperties.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE.equalsIgnoreCase(
        jcsmpProperties.getStringProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
  }

  @Test
  void canConnectWhenAuthenticationSchemeIsClientCertificate() {
    if (!isClientCertificateAuthentication()) {
      fail("Was expecting the authentication scheme to be client certificate.");
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
}