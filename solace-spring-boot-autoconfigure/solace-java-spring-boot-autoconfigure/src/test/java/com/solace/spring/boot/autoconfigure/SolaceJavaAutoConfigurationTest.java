/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.solace.spring.boot.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

class SolaceJavaAutoConfigurationTest {

  @Configuration public static class EmptyConfiguration { }

  private AnnotationConfigApplicationContext context;
  private final Class<SolaceJavaAutoConfiguration> configClass = SolaceJavaAutoConfiguration.class;

  @AfterEach
  void tearDown() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  void defaultNativeConnectionFactory() throws InvalidPropertiesException {
    load("");
    SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
    assertNotNull(jcsmpFactory);

    JCSMPSession session = jcsmpFactory.createSession();
    assertNotNull(session);

    assertEquals("localhost", (String) session.getProperty(JCSMPProperties.HOST));
    assertEquals("default", (String) session.getProperty(JCSMPProperties.VPN_NAME));
    assertEquals("spring-default-client-username",
        (String) session.getProperty(JCSMPProperties.USERNAME));
    assertEquals("", (String) session.getProperty(JCSMPProperties.PASSWORD));
    assertEquals(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO,
        (String) session.getProperty(JCSMPProperties.MESSAGE_ACK_MODE));
    assertEquals(Boolean.FALSE,
        (Boolean) session.getProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS));
    assertNotNull((String) session.getProperty(JCSMPProperties.CLIENT_NAME));
    // Channel properties
    JCSMPChannelProperties cp = (JCSMPChannelProperties) session
        .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
    assertEquals(1, (int) cp.getConnectRetries());
    assertEquals(-1, (int) cp.getReconnectRetries());
    assertEquals(20, (int) cp.getConnectRetriesPerHost());
    assertEquals(3000, (int) cp.getReconnectRetryWaitInMillis());
  }

  @Test
  void customNativeConnectionFactory() throws InvalidPropertiesException {
    load("solace.java.host=192.168.1.80:55500",
        "solace.java.clientUsername=bob", "solace.java.clientPassword=password",
        "solace.java.msgVpn=newVpn", "solace.java.clientName=client-name",
        "solace.java.connectRetries=5", "solace.java.reconnectRetries=10",
        "solace.java.connectRetriesPerHost=40", "solace.java.reconnectRetryWaitInMillis=1000",
        "solace.java.messageAckMode=client_ack", "solace.java.reapplySubscriptions=true",
        "solace.java.advanced.jcsmp.TOPIC_DISPATCH=true");

    SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
    assertNotNull(jcsmpFactory);
    JCSMPSession session = jcsmpFactory.createSession();
    assertNotNull(session);

    assertEquals("192.168.1.80:55500", (String) session.getProperty(JCSMPProperties.HOST));
    assertEquals("newVpn", (String) session.getProperty(JCSMPProperties.VPN_NAME));
    assertEquals("bob", (String) session.getProperty(JCSMPProperties.USERNAME));
    assertEquals("password", (String) session.getProperty(JCSMPProperties.PASSWORD));
    assertEquals("client-name", (String) session.getProperty(JCSMPProperties.CLIENT_NAME));
    // Channel properties
    JCSMPChannelProperties cp = (JCSMPChannelProperties) session
        .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
    assertEquals(5, (int) cp.getConnectRetries());
    assertEquals(10, (int) cp.getReconnectRetries());
    assertEquals(40, (int) cp.getConnectRetriesPerHost());
    assertEquals(1000, (int) cp.getReconnectRetryWaitInMillis());
  }

  void load(String... environment) {
    load(EmptyConfiguration.class, environment);
  }

  void load(Class<?> config, String... environment) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    TestPropertyValues.of(environment).applyTo(applicationContext);
    applicationContext.register(config);
    applicationContext.register(configClass);
    applicationContext.refresh();
    this.context = applicationContext;
  }
}
