package com.solace.spring.boot.autoconfigure.springBootTests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.File;
import java.time.Duration;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.Wait;

public interface MessagingServiceFreeTierBrokerTestContainerWithTlsSetup {

  String FULL_DOCKER_COMPOSE_FILE_PATH = "src/test/resources/free-tier-broker-with-tls-docker-compose.yml";
  String PUBSUB_BROKER_SERVICE_NAME = "solbroker";

  Logger LOGGER = LoggerFactory.getLogger(
      MessagingServiceFreeTierBrokerTestContainerWithTlsSetup.class);

  ComposeContainer COMPOSE_CONTAINER = new ComposeContainer(
      new File(FULL_DOCKER_COMPOSE_FILE_PATH)).withLocalCompose(true).withPull(true)
      .withExposedService(PUBSUB_BROKER_SERVICE_NAME, 8080)
      .withExposedService(PUBSUB_BROKER_SERVICE_NAME, 55443)
      .withExposedService(PUBSUB_BROKER_SERVICE_NAME, 55555)

      .waitingFor(PUBSUB_BROKER_SERVICE_NAME,
          Wait.forHttp("/").forPort(8080).withStartupTimeout(Duration.ofSeconds(120)));

  @BeforeAll
  static void startContainer() {
    System.setProperty("javax.net.ssl.trustStore",
        new File("src/test/resources/certs/client/client-truststore.p12").getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStorePassword", "changeMe123");
    System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
    //System.setProperty("javax.net.debug", "all");

    COMPOSE_CONTAINER.start();
  }

  @BeforeAll
  static void checkContainer() {
    String solaceBroker = COMPOSE_CONTAINER.getServiceHost(PUBSUB_BROKER_SERVICE_NAME, 8080);
    assertNotNull(solaceBroker, "solace broker host expected to be not null");
  }

  @AfterAll
  static void afterAll() {
    final SolaceBroker broker = SolaceBroker.getInstance();
    broker.backupFinalBrokerLogs(); //Backup container logs before it's destroyed
    COMPOSE_CONTAINER.stop();  //Destroy the container
  }

  class SolaceBroker {

    private static final class LazyHolder {

      static final SolaceBroker INSTANCE = new SolaceBroker();
    }


    public static SolaceBroker getInstance() {
      return LazyHolder.INSTANCE;
    }

    private final ComposeContainer container;

    private SolaceBroker(ComposeContainer container) {
      this.container = container;
    }

    public SolaceBroker() {
      this(COMPOSE_CONTAINER);
    }

    /**
     * bucks up final log form a broker
     */
    void backupFinalBrokerLogs() {
      final Consumer<ContainerState> copyToBrokerJob = containerState -> {
        if (containerState.isRunning()) {
          try {
            containerState.copyFileFromContainer("/usr/sw/jail/logs/debug.log",
                "clientCertAuth_test_final_debug.log");
            containerState.copyFileFromContainer("/usr/sw/jail/logs/event.log",
                "clientCertAuth_test_final_event.log");
          } catch (Exception e) {
            LOGGER.error("Failed to backup final log from a broker", e);
          }
        }
      };
      // run actual job on a container
      container.getContainerByServiceName(PUBSUB_BROKER_SERVICE_NAME + "_1")
          .ifPresent(copyToBrokerJob);
    }
  }
}