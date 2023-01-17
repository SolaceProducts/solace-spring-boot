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

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
public class SolaceJavaAutoConfigurationTest extends SolaceJavaAutoConfigurationTestBase {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    public SolaceJavaAutoConfigurationTest() {
        super(SolaceJavaAutoConfiguration.class);
    }

    @Test
    void defaultNativeConnectionFactory() throws InvalidPropertiesException {
        load("");
        SpringJCSMPFactory jcsmpFactory = context.getBean(SpringJCSMPFactory.class);
        assertNotNull(jcsmpFactory);

        JCSMPSession session = jcsmpFactory.createSession();
        assertNotNull(session);

        assertEquals("localhost", session.getProperty(JCSMPProperties.HOST));
        assertEquals("default", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("spring-default-client-username", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("", session.getProperty(JCSMPProperties.PASSWORD));
        assertEquals(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO, session.getProperty(JCSMPProperties.MESSAGE_ACK_MODE));
        assertEquals(Boolean.FALSE, session.getProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS));
        assertNotNull(session.getProperty(JCSMPProperties.CLIENT_NAME));
        // Channel properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) session
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        assertEquals(1, cp.getConnectRetries());
        assertEquals(5, cp.getReconnectRetries());
        assertEquals(20, cp.getConnectRetriesPerHost());
        assertEquals(3000, cp.getReconnectRetryWaitInMillis());
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

        SpringJCSMPFactory jcsmpFactory = context.getBean(SpringJCSMPFactory.class);
        assertNotNull(jcsmpFactory);
        JCSMPSession session = jcsmpFactory.createSession();
        assertNotNull(session);

        assertEquals("192.168.1.80:55500", session.getProperty(JCSMPProperties.HOST));
        assertEquals("newVpn", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("bob", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("password", session.getProperty(JCSMPProperties.PASSWORD));
        assertEquals("client-name", session.getProperty(JCSMPProperties.CLIENT_NAME));
        // Channel properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) session
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        assertEquals(5, cp.getConnectRetries());
        assertEquals(10, cp.getReconnectRetries());
        assertEquals(40, cp.getConnectRetriesPerHost());
        assertEquals(1000, cp.getReconnectRetryWaitInMillis());
    }

    @Test
    void externallyLoadedServicePropertiesBasicBeanTest() {
        // Testing one type of externally loaded service is good enough
        // The loader has its own tests for the other scenarios
        String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";

        EnvConfig configuration = getOneSolaceService(ENV_SOLCAP_SERVICES);
        environmentVariables.set(configuration.envName(), configuration.envValue());

        load(String.format("%s=%s", ENV_SOLCAP_SERVICES, configuration.envValue()));

        String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
        assertNotNull(solaceManifest);
        assertTrue(solaceManifest.contains("solace-pubsub"));

        assertNotNull(context.getBean(SpringJCSMPFactoryCloudFactory.class));
        assertNotNull(context.getBean(SpringJCSMPFactory.class));
        assertNotNull(context.getBean(JCSMPProperties.class));
        assertNotNull(context.getBean(SolaceServiceCredentials.class));
    }

    @Test
    void noExternallyLoadedServicePropertiesBasicBeanTest() {
        // Testing one type of externally loaded service is good enough
        // The loader has its own tests for the other scenarios
        String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";
        load(String.format("%s={ \"solace-pubsub\": [] }", ENV_SOLCAP_SERVICES));

        String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
        assertNotNull(solaceManifest);
        assertTrue(solaceManifest.contains("solace-pubsub"));

        assertNotNull(context.getBean(SpringJCSMPFactoryCloudFactory.class));
        assertNotNull(context.getBean(SpringJCSMPFactory.class));
        assertNotNull(context.getBean(JCSMPProperties.class));
        NoSuchBeanDefinitionException thrown = assertThrows(NoSuchBeanDefinitionException.class, () ->
                context.getBean(SolaceServiceCredentials.class));
        assertEquals(SolaceServiceCredentials.class, thrown.getBeanType());
    }

    @Test
    void applicationPropertiesBasicBeanTest() {
        load("");
        assertNotNull(context.getBean(SpringJCSMPFactoryCloudFactory.class));
        assertNotNull(context.getBean(SpringJCSMPFactory.class));
        assertNotNull(context.getBean(JCSMPProperties.class));
        NoSuchBeanDefinitionException thrown = assertThrows(NoSuchBeanDefinitionException.class, () ->
                context.getBean(SolaceServiceCredentials.class));
        assertEquals(SolaceServiceCredentials.class, thrown.getBeanType());
    }

}
