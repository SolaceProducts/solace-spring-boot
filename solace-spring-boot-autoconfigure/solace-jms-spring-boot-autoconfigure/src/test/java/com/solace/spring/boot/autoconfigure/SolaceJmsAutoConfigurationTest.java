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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

class SolaceJmsAutoConfigurationTest {

  @Configuration public static class EmptyConfiguration {}
  private AnnotationConfigApplicationContext context;
  private final Class<SolaceJmsAutoConfiguration> configClass = SolaceJmsAutoConfiguration.class;

  @AfterEach
  public void tearDown() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  void defaultNativeConnectionFactory() {
    load("");
    JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
    SolConnectionFactoryImpl connectionFactory = this.context
        .getBean(SolConnectionFactoryImpl.class);
    assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
    assertEquals("tcp://localhost", connectionFactory.getHost());
    assertEquals("default", connectionFactory.getVPN());
    assertEquals("spring-default-client-username", connectionFactory.getUsername());
    assertEquals("", connectionFactory.getPassword());
    assertFalse(connectionFactory.getDirectTransport());
  }

  @Test
  void customNativeConnectionFactory() {
    load("solace.jms.host=192.168.1.80:55500",
        "solace.jms.clientUsername=bob", "solace.jms.clientPassword=password",
        "solace.jms.msgVpn=newVpn");
    JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
    SolConnectionFactoryImpl connectionFactory = this.context.getBean(
        SolConnectionFactoryImpl.class);
    assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
    assertEquals("tcp://192.168.1.80:55500", connectionFactory.getHost());
    assertEquals("newVpn", connectionFactory.getVPN());
    assertEquals("bob", connectionFactory.getUsername());
    assertEquals("password", connectionFactory.getPassword());
    assertFalse(connectionFactory.getDirectTransport());
  }

  void load(String... environment) {
    load(EmptyConfiguration.class, environment);
  }

  void load(Class<?> config, String... environment) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    TestPropertyValues.of(environment).applyTo(applicationContext);
    applicationContext.register(config);
    applicationContext.register(configClass, JmsAutoConfiguration.class);
    applicationContext.refresh();
    this.context = applicationContext;
  }
}