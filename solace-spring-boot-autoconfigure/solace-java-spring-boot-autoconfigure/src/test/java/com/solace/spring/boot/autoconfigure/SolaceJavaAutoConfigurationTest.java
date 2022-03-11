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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class SolaceJavaAutoConfigurationTest extends SolaceJavaAutoConfigurationTestBase {

	public SolaceJavaAutoConfigurationTest() {
		super(SolaceJavaAutoConfiguration.class);
	}

	@Test
	public void defaultNativeConnectionFactory() throws InvalidPropertiesException {
		load("");
		SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
		assertNotNull(jcsmpFactory);

		JCSMPSession session = jcsmpFactory.createSession();
		assertNotNull(session);

		assertEquals("localhost", (String)session.getProperty(JCSMPProperties.HOST));
        assertEquals("default", (String)session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("spring-default-client-username", (String)session.getProperty(JCSMPProperties.USERNAME) );
        assertEquals("", (String)session.getProperty(JCSMPProperties.PASSWORD));
        assertEquals(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO,(String)session.getProperty(JCSMPProperties.MESSAGE_ACK_MODE));
        assertEquals(Boolean.FALSE,(Boolean) session.getProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS));
        assertNotNull((String)session.getProperty(JCSMPProperties.CLIENT_NAME));
        // Channel properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) session
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        assertEquals(1, (int)cp.getConnectRetries());
        assertEquals(5, (int)cp.getReconnectRetries());
        assertEquals(20, (int)cp.getConnectRetriesPerHost());
        assertEquals(3000, (int)cp.getReconnectRetryWaitInMillis());
    }

	@Test
	public void customNativeConnectionFactory() throws InvalidPropertiesException {
		load("solace.java.host=192.168.1.80:55500",
				"solace.java.clientUsername=bob", "solace.java.clientPassword=password",
				"solace.java.msgVpn=newVpn", "solace.java.clientName=client-name",
				"solace.java.connectRetries=5", "solace.java.reconnectRetries=10",
				"solace.java.connectRetriesPerHost=40", "solace.java.reconnectRetryWaitInMillis=1000",
				"solace.java.messageAckMode=client_ack","solace.java.reapplySubscriptions=true",
				"solace.java.advanced.jcsmp.TOPIC_DISPATCH=true");

		SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
		assertNotNull(jcsmpFactory);
        JCSMPSession session = jcsmpFactory.createSession();
        assertNotNull(session);

        assertEquals("192.168.1.80:55500", (String)session.getProperty(JCSMPProperties.HOST));
        assertEquals("newVpn", (String)session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("bob", (String)session.getProperty(JCSMPProperties.USERNAME) );
        assertEquals("password", (String)session.getProperty(JCSMPProperties.PASSWORD) );
        assertEquals("client-name", (String)session.getProperty(JCSMPProperties.CLIENT_NAME) );
        // Channel properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) session
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        assertEquals(5, (int)cp.getConnectRetries());
        assertEquals(10, (int)cp.getReconnectRetries());
        assertEquals(40, (int)cp.getConnectRetriesPerHost());
        assertEquals(1000, (int)cp.getReconnectRetryWaitInMillis());
	}

	@Test
	public void externallyLoadedServicePropertiesBasicBeanTest() {
		// Testing one type of externally loaded service is good enough
		// The loader has its own tests for the other scenarios
		String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";

		load(String.format("%s={ \"solace-pubsub\": [%s] }",
				ENV_SOLCAP_SERVICES, addOneSolaceService(ENV_SOLCAP_SERVICES)));

		String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
		assertNotNull(solaceManifest);
		assertTrue(solaceManifest.contains("solace-pubsub"));

		assertNotNull(this.context.getBean(SpringJCSMPFactoryCloudFactory.class));
		assertNotNull(this.context.getBean(SpringJCSMPFactory.class));
		assertNotNull(this.context.getBean(JCSMPProperties.class));
		assertNotNull(this.context.getBean(SolaceServiceCredentials.class));
	}

	@Test
	public void noExternallyLoadedServicePropertiesBasicBeanTest() {
		// Testing one type of externally loaded service is good enough
		// The loader has its own tests for the other scenarios
		String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";
		load(String.format("%s={ \"solace-pubsub\": [] }", ENV_SOLCAP_SERVICES));

		String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
		assertNotNull(solaceManifest);
		assertTrue(solaceManifest.contains("solace-pubsub"));

		assertNotNull(this.context.getBean(SpringJCSMPFactoryCloudFactory.class));
		assertNotNull(this.context.getBean(SpringJCSMPFactory.class));
		assertNotNull(this.context.getBean(JCSMPProperties.class));
		NoSuchBeanDefinitionException thrown = assertThrows(NoSuchBeanDefinitionException.class, () ->
				this.context.getBean(SolaceServiceCredentials.class));
		assertEquals(SolaceServiceCredentials.class, thrown.getBeanType());
	}

	@Test
	public void applicationPropertiesBasicBeanTest() {
		load("");
		assertNotNull(this.context.getBean(SpringJCSMPFactoryCloudFactory.class));
		assertNotNull(this.context.getBean(SpringJCSMPFactory.class));
		assertNotNull(this.context.getBean(JCSMPProperties.class));
		NoSuchBeanDefinitionException thrown = assertThrows(NoSuchBeanDefinitionException.class, () ->
				this.context.getBean(SolaceServiceCredentials.class));
		assertEquals(SolaceServiceCredentials.class, thrown.getBeanType());
	}

}
