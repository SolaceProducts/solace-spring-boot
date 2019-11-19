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
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import org.junit.Test;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SolaceJmsAutoConfigurationTest extends SolaceJmsAutoConfigurationTestBase {
	public SolaceJmsAutoConfigurationTest() {
		super(SolaceJmsAutoConfiguration.class);
	}

	@Test
	public void defaultNativeConnectionFactory() {
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
	public void customNativeConnectionFactory() {
		load("solace.jms.host=192.168.1.80:55500",
				"solace.jms.clientUsername=bob", "solace.jms.clientPassword=password",
				"solace.jms.msgVpn=newVpn");
		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
		SolConnectionFactoryImpl connectionFactory = this.context.getBean(SolConnectionFactoryImpl.class);
		assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
		assertEquals("tcp://192.168.1.80:55500", connectionFactory.getHost());
        assertEquals("newVpn", connectionFactory.getVPN());
        assertEquals("bob", connectionFactory.getUsername());
        assertEquals("password", connectionFactory.getPassword());
        assertFalse(connectionFactory.getDirectTransport());
	}

    @Test
    public void externallyLoadedServiceProperties() {
        // Testing one type of externally loaded service is good enough
        // The loader has its own tests for the other scenarios
        String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";

        load(String.format("%s={ \"solace-pubsub\": [%s] }",
                ENV_SOLCAP_SERVICES, addOneSolaceService(ENV_SOLCAP_SERVICES)));

        String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
        assertNotNull(solaceManifest);
        assertTrue(solaceManifest.contains("solace-pubsub"));

        assertNotNull(this.context.getBean(SolConnectionFactory.class));
        assertNotNull(this.context.getBean(SpringSolJmsConnectionFactoryCloudFactory.class));
        assertNotNull(this.context.getBean(SolaceServiceCredentials.class));
    }

	@Test
	public void noExternallyLoadedServiceProperties() {
		// Testing one type of externally loaded service is good enough
		// The loader has its own tests for the other scenarios
		String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";
		load(String.format("%s={ \"solace-pubsub\": [] }", ENV_SOLCAP_SERVICES));

		String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
		assertNotNull(solaceManifest);
		assertTrue(solaceManifest.contains("solace-pubsub"));

		assertNotNull(this.context.getBean(SolConnectionFactory.class));
		assertNotNull(this.context.getBean(SpringSolJmsConnectionFactoryCloudFactory.class));
		try {
			assertNull(this.context.getBean(SolaceServiceCredentials.class));
		} catch (BeanNotOfRequiredTypeException e) {
			assert(e.getMessage().contains("was actually of type 'org.springframework.beans.factory.support.NullBean'"));
		}
	}
}
