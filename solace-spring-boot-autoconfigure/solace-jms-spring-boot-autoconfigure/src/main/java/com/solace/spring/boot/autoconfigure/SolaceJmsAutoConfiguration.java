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

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.property.JMSProperties;
import jakarta.jms.ConnectionFactory;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, SolConnectionFactory.class})
@ConditionalOnMissingBean({ConnectionFactory.class, JndiTemplate.class})
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJmsAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(
      SolaceJmsAutoConfiguration.class);

  private SolaceJmsProperties properties;

  @Autowired
  public SolaceJmsAutoConfiguration(SolaceJmsProperties properties) {
    this.properties = properties;
  }

  /**
   * Returns a {@link SolConnectionFactory} based on the {@link SolaceJmsProperties}.
   *
   * @return {@link SolConnectionFactory} based on the {@link SolaceJmsProperties}
   */
  @Bean
  public SolConnectionFactory getSolConnectionFactory() {
    try {
      Hashtable<String, String> ht = new Hashtable<>(properties.getApiProperties());
      JMSProperties props = new JMSProperties(ht);
      props.initialize();
      SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);
      cf.setHost(properties.getHost());
      cf.setVPN(properties.getMsgVpn());
      cf.setUsername(properties.getClientUsername());
      cf.setPassword(properties.getClientPassword());
      cf.setDirectTransport(properties.isDirectTransport());
      return cf;
    } catch (Exception ex) {
      logger.error("Exception found during Solace Connection Factory creation.", ex);
      throw new IllegalStateException("Unable to create Solace connection factory, "
          + "ensure that the sol-jms-jakarta-<version>.jar "
          + "is the classpath", ex);
    }
  }

  /**
   * Returns a {@link JndiTemplate} based on the {@link SolaceJmsProperties}.
   *
   * @return {@link JndiTemplate} based on the {@link SolaceJmsProperties}
   */
  @Bean
  public JndiTemplate getJndiTemplate() {
    try {
      Properties env = new Properties();
      env.putAll(properties.getApiProperties());
      env.put(InitialContext.INITIAL_CONTEXT_FACTORY,
          "com.solacesystems.jndi.SolJNDIInitialContextFactory");
      env.put(InitialContext.PROVIDER_URL, properties.getHost());
      env.put(Context.SECURITY_PRINCIPAL,
          properties.getClientUsername() + '@' + properties.getMsgVpn());
      env.put(Context.SECURITY_CREDENTIALS, properties.getClientPassword());

      JndiTemplate jndiTemplate = new JndiTemplate();
      jndiTemplate.setEnvironment(env);
      return jndiTemplate;
    } catch (Exception ex) {
      logger.error("Exception found during Solace JNDI Initial Context creation.", ex);
      throw new IllegalStateException("Unable to create Solace "
          + "JNDI Initial Context, ensure that the sol-jms-jakarta-<version>.jar "
          + "is the classpath", ex);
    }
  }

  void setProperties(SolaceJmsProperties properties) {
    this.properties = properties;
  }
}