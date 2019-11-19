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
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public class SolaceJmsAutoCloudConfigurationTest<T> extends SolaceJmsAutoConfigurationTestBase {

	// Just enough to satisfy the Cloud Condition we need
	private static String CF_CLOUD_APP_ENV = "VCAP_APPLICATION={}";

	// Some other Service
	private static String CF_VCAP_SERVICES_OTHER = "VCAP_SERVICES={ otherService: [ { id: '1' } , { id: '2' } ]}";

    @Parameter(0) public String beanClassName;
    @Parameter(1) public Class<T> beanClass;

    public SolaceJmsAutoCloudConfigurationTest() {
        super(SolaceJmsAutoCloudConfiguration.class);
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> parameterData() {
        Set<ResolvableType> classes = new HashSet<>();
        classes.add(ResolvableType.forClass(SpringSolJmsConnectionFactoryCloudFactory.class));
        classes.add(ResolvableType.forClass(SolConnectionFactory.class));
        classes.add(ResolvableType.forClass(SolaceServiceCredentials.class));
        return getTestParameters(classes);
    }

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noBeanNotCloud() throws NoSuchBeanDefinitionException {
		try {
			load("");
			this.context.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException e) {
			assertTrue(e.getBeanType().isAssignableFrom(beanClass));
			throw e;
		}
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noBeanIsCloudNoService() throws NoSuchBeanDefinitionException {
		load(CF_CLOUD_APP_ENV);

		Environment env = context.getEnvironment();
		String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
		assertNotNull(VCAP_APPLICATION);
		assertEquals("{}", VCAP_APPLICATION);

		String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
		assertNull(VCAP_SERVICES);

		try {
			this.context.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException e) {
			assertTrue(e.getBeanType().isAssignableFrom(beanClass));
			throw e;
		}
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noBeanIsCloudWrongService() throws NoSuchBeanDefinitionException {
		load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES_OTHER);

		Environment env = context.getEnvironment();
		String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
		assertNotNull(VCAP_APPLICATION);
		assertEquals("{}", VCAP_APPLICATION);

		String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
		assertNotNull(VCAP_SERVICES);
		assertFalse(VCAP_SERVICES.contains("solace-pubsub"));

		try {
			this.context.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException e) {
			assertTrue(e.getBeanType().isAssignableFrom(beanClass));
			throw e;
		}
	}

	private void makeCloudEnv() {
		// To force the detection of spring cloud connector which uses
		// EnvironmentAccessor
		environmentVariables.set("VCAP_APPLICATION", "{}");
		assertEquals("{}", System.getenv("VCAP_APPLICATION"));
	}

	@Test
	public void hasBeanIsCloudHasService() throws NoSuchBeanDefinitionException {

		makeCloudEnv();

		String JSONString = addOneSolaceService("VCAP_SERVICES");
		String CF_VCAP_SERVICES = "VCAP_SERVICES={ \"solace-pubsub\": [" + JSONString + "] }";

		load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

		Environment env = context.getEnvironment();

		String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
		assertNotNull(VCAP_APPLICATION);
		assertEquals("{}", VCAP_APPLICATION);

		String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
		assertNotNull(VCAP_SERVICES);
		assertTrue(VCAP_SERVICES.contains("solace-pubsub"));

		T bean = this.context.getBean(beanClass);
		assertNotNull(bean);

		if (beanClass.equals(SpringSolJmsConnectionFactoryCloudFactory.class)) {
                    SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory =
                            (SpringSolJmsConnectionFactoryCloudFactory) bean;
                    assertNotNull(springSolConnectionFactoryCloudFactory.getSolConnectionFactory());
                    List<SolaceServiceCredentials> availableServices = springSolConnectionFactoryCloudFactory
                            .getSolaceServiceCredentials();
                    assertNotNull(availableServices);
                    assertEquals(1,availableServices.size());
                }
	}

	@Test
	public void isCloudConfiguredBySolaceMessagingInfoAndDefaultsForOtherProperties() {

		makeCloudEnv();

		String JSONString = addOneSolaceService("VCAP_SERVICES");
		String CF_VCAP_SERVICES = "VCAP_SERVICES={ \"solace-pubsub\": [" + JSONString + "] }";

		load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

		SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = this.context
				.getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
		assertNotNull(springSolConnectionFactoryCloudFactory);

		SolConnectionFactory solConnectionFactory = this.context.getBean(SolConnectionFactory.class);
		assertNotNull(solConnectionFactory);

		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);

		assertEquals(jmsTemplate.getConnectionFactory(), solConnectionFactory);
		assertEquals("tcp://192.168.1.50:7000", solConnectionFactory.getHost());
		assertEquals("sample-msg-vpn", solConnectionFactory.getVPN());
		assertEquals("sample-client-username", solConnectionFactory.getUsername());
		assertEquals("sample-client-password", solConnectionFactory.getPassword());
		assertFalse(solConnectionFactory.getDirectTransport());

	}

        @Test
        public void isCloudConfiguredByUserProvidedServices() {

                makeCloudEnv();

                String JSONString = addOneUserProvidedSolaceService("VCAP_SERVICES");
                String CF_VCAP_SERVICES = "VCAP_SERVICES={ \"user-provided\": [" + JSONString + "] }";

                load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

                Environment env = context.getEnvironment();

                String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
                assertNotNull(VCAP_APPLICATION);
                assertEquals("{}", VCAP_APPLICATION);

                String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
                assertNotNull(VCAP_SERVICES);
                assertTrue(VCAP_SERVICES.contains("solace-pubsub"));

                T bean = this.context.getBean(beanClass);
                assertNotNull(bean);

                if (beanClass.equals(SpringSolJmsConnectionFactoryCloudFactory.class)) {
                    SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = (SpringSolJmsConnectionFactoryCloudFactory) bean;
                    assertNotNull(springSolConnectionFactoryCloudFactory.getSolConnectionFactory());
                    List<SolaceServiceCredentials> availableServices = springSolConnectionFactoryCloudFactory
                            .getSolaceServiceCredentials();
                    assertNotNull(availableServices);
                    assertEquals(1, availableServices.size());
                }
        }

        @Test
	public void isCloudConfiguredBySolaceMessagingInfoAndOtherProperties() {
		makeCloudEnv();

		String JSONString = addOneSolaceService("VCAP_SERVICES");
		String CF_VCAP_SERVICES = "VCAP_SERVICES={ \"solace-pubsub\": [" + JSONString + "] }";

		load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.jms.host=192.168.1.80:55500",
				"solace.jms.clientUsername=bob", "solace.jms.clientPassword=password", "solace.jms.msgVpn=newVpn");

		SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = this.context
				.getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
		assertNotNull(springSolConnectionFactoryCloudFactory);

		SolConnectionFactory solConnectionFactory = this.context.getBean(SolConnectionFactory.class);
		assertNotNull(solConnectionFactory);

		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);

		// Notice that the other properties where ineffective
		assertEquals(jmsTemplate.getConnectionFactory(), solConnectionFactory);
		assertEquals("tcp://192.168.1.50:7000", solConnectionFactory.getHost());
		assertEquals("sample-msg-vpn", solConnectionFactory.getVPN());
		assertEquals("sample-client-username", solConnectionFactory.getUsername());
		assertEquals("sample-client-password", solConnectionFactory.getPassword());
		assertFalse(solConnectionFactory.getDirectTransport());

	}

	@Test
	public void isCloudConfiguredBySolaceMessagingInfoAndOtherPropertiesWhenMissingCredentials() {

		makeCloudEnv();

		Map<String, Object> services = createOneService();
		@SuppressWarnings("unchecked")
		Map<String, Object> credentials = (Map<String, Object>) services.get("credentials");
		credentials.remove("clientUsername");
		credentials.remove("clientPassword");

		JSONObject jsonMapObject = new JSONObject(services);
		String JSONString = jsonMapObject.toString();
		environmentVariables.set("VCAP_SERVICES", "{ \"solace-pubsub\": [" + JSONString + "] }");

		String CF_VCAP_SERVICES = "VCAP_SERVICES={ \"solace-pubsub\": [" + JSONString + "] }";

		load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.jms.host=192.168.1.80:55500",
				"solace.jms.clientUsername=bob", "solace.jms.clientPassword=password", "solace.jms.msgVpn=newVpn");

		SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = this.context
				.getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
		assertNotNull(springSolConnectionFactoryCloudFactory);

		SolConnectionFactory solConnectionFactory = this.context.getBean(SolConnectionFactory.class);
		assertNotNull(solConnectionFactory);

		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);

		// Notice that the other properties where ineffective, only the username
		// and password where effective in this case.
		assertEquals(jmsTemplate.getConnectionFactory(), solConnectionFactory);
		assertEquals("tcp://192.168.1.50:7000", solConnectionFactory.getHost());
		assertEquals("sample-msg-vpn", solConnectionFactory.getVPN());
		assertEquals("bob", solConnectionFactory.getUsername());
		assertEquals("password", solConnectionFactory.getPassword());
		assertFalse(solConnectionFactory.getDirectTransport());
	}
}