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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
public class SolaceJmsAutoCloudConfigurationTest<T> extends SolaceJmsAutoConfigurationTestBase {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private static final String VCAP_SVC_ENV = "VCAP_SERVICES";
    private static final String VCAP_APP_ENV = "VCAP_APPLICATION";

    // Just enough to satisfy the Cloud Condition we need
    private static final String CF_CLOUD_APP_ENV = VCAP_APP_ENV + "={}";

    // Some other Service
    private static final String CF_VCAP_SERVICES_OTHER = VCAP_SVC_ENV + "={ otherService: [ { id: '1' } , { id: '2' } ]}";

    public SolaceJmsAutoCloudConfigurationTest() {
        super(SolaceJmsAutoCloudConfiguration.class);
    }

    public static Collection<Object[]> parameterData() {
        Set<ResolvableType> classes = new HashSet<>();
        classes.add(ResolvableType.forClass(SpringSolJmsConnectionFactoryCloudFactory.class));
        classes.add(ResolvableType.forClass(SolConnectionFactory.class));
        classes.add(ResolvableType.forClass(SolaceServiceCredentials.class));
        return getTestParameters(classes);
    }

    @ParameterizedTest
    @MethodSource("parameterData")
    void noBeanNotCloud(String beanClassName, Class<?> beanClass) {
        load("");
        verifyNoSuchBeanDefinitionOnGetBean(beanClass);
    }

    private void verifyNoSuchBeanDefinitionOnGetBean(Class<?> beanClass) {
        NoSuchBeanDefinitionException exception = assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(beanClass));
        Assertions.assertTrue(exception.getBeanType().isAssignableFrom(beanClass));
    }

    @ParameterizedTest
    @MethodSource("parameterData")
    void noBeanIsCloudNoService(String beanClassName, Class<?> beanClass) {
        load(CF_CLOUD_APP_ENV);

        Environment env = context.getEnvironment();
        String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
        assertNotNull(VCAP_APPLICATION);
        assertEquals("{}", VCAP_APPLICATION);

        String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
        assertNull(VCAP_SERVICES);

        verifyNoSuchBeanDefinitionOnGetBean(beanClass);
    }

    @ParameterizedTest
    @MethodSource("parameterData")
    void noBeanIsCloudWrongService(String beanClassName, Class<?> beanClass) {
        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES_OTHER);

        Environment env = context.getEnvironment();
        String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
        assertNotNull(VCAP_APPLICATION);
        assertEquals("{}", VCAP_APPLICATION);

        String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
        assertNotNull(VCAP_SERVICES);
        assertFalse(VCAP_SERVICES.contains("solace-pubsub"));

        verifyNoSuchBeanDefinitionOnGetBean(beanClass);
    }

    private void makeCloudEnv(String vcapService) {
        environmentVariables.set(VCAP_APP_ENV, "{}");
        assertEquals("{}", System.getenv(VCAP_APP_ENV));
        environmentVariables.set(VCAP_SVC_ENV, vcapService);
        assertEquals(vcapService, System.getenv(VCAP_SVC_ENV));
    }

    @ParameterizedTest
    @MethodSource("parameterData")
    void hasBeanIsCloudHasService(String beanClassName, Class<T> beanClass) {
        EnvConfig configuration = getOneSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

        Environment env = context.getEnvironment();

        String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
        assertNotNull(VCAP_APPLICATION);
        assertEquals("{}", VCAP_APPLICATION);

        String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
        assertNotNull(VCAP_SERVICES);
        assertTrue(VCAP_SERVICES.contains("solace-pubsub"));

        T bean = context.getBean(beanClass);
        assertNotNull(bean);

        if (beanClass.equals(SpringSolJmsConnectionFactoryCloudFactory.class)) {
            SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory =
                    (SpringSolJmsConnectionFactoryCloudFactory) bean;
            assertNotNull(springSolConnectionFactoryCloudFactory.getSolConnectionFactory());
            List<SolaceServiceCredentials> availableServices = springSolConnectionFactoryCloudFactory
                    .getSolaceServiceCredentials();
            assertNotNull(availableServices);
            assertEquals(1, availableServices.size());
        }
    }

    @Test
    void isCloudConfiguredBySolaceMessagingInfoAndDefaultsForOtherProperties() {
        EnvConfig configuration = getOneSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

        SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = context
                .getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
        assertNotNull(springSolConnectionFactoryCloudFactory);

        SolConnectionFactory solConnectionFactory = context.getBean(SolConnectionFactory.class);
        assertNotNull(solConnectionFactory);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        assertEquals(jmsTemplate.getConnectionFactory(), solConnectionFactory);
        assertEquals("tcp://192.168.1.50:7000", solConnectionFactory.getHost());
        assertEquals("sample-msg-vpn", solConnectionFactory.getVPN());
        assertEquals("sample-client-username", solConnectionFactory.getUsername());
        assertEquals("sample-client-password", solConnectionFactory.getPassword());
        assertFalse(solConnectionFactory.getDirectTransport());
    }

    @ParameterizedTest
    @MethodSource("parameterData")
    void isCloudConfiguredByUserProvidedServices(String beanClassName, Class<T> beanClass) {
        EnvConfig configuration = getOneUserProvidedSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

        Environment env = context.getEnvironment();

        String VCAP_APPLICATION = env.getProperty("VCAP_APPLICATION");
        assertNotNull(VCAP_APPLICATION);
        assertEquals("{}", VCAP_APPLICATION);

        String VCAP_SERVICES = env.getProperty("VCAP_SERVICES");
        assertNotNull(VCAP_SERVICES);
        assertTrue(VCAP_SERVICES.contains("solace-pubsub"));

        T bean = context.getBean(beanClass);
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
    void isCloudConfiguredBySolaceMessagingInfoAndOtherProperties() {
        EnvConfig configuration = getOneSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.jms.host=192.168.1.80:55500",
                "solace.jms.clientUsername=bob", "solace.jms.clientPassword=password", "solace.jms.msgVpn=newVpn");

        SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = context
                .getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
        assertNotNull(springSolConnectionFactoryCloudFactory);

        SolConnectionFactory solConnectionFactory = context.getBean(SolConnectionFactory.class);
        assertNotNull(solConnectionFactory);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        // Notice that the other properties where ineffective
        assertEquals(jmsTemplate.getConnectionFactory(), solConnectionFactory);
        assertEquals("tcp://192.168.1.50:7000", solConnectionFactory.getHost());
        assertEquals("sample-msg-vpn", solConnectionFactory.getVPN());
        assertEquals("sample-client-username", solConnectionFactory.getUsername());
        assertEquals("sample-client-password", solConnectionFactory.getPassword());
        assertFalse(solConnectionFactory.getDirectTransport());
    }

    @Test
    void isCloudConfiguredBySolaceMessagingInfoAndOtherPropertiesWhenMissingCredentials() {
        Map<String, Object> services = createOneService();
        @SuppressWarnings("unchecked")
        Map<String, Object> credentials = (Map<String, Object>) services.get("credentials");
        credentials.remove("clientUsername");
        credentials.remove("clientPassword");

        JSONObject jsonMapObject = new JSONObject(services);
        String JSONString = jsonMapObject.toString();
        String vcapServices = "{ \"solace-pubsub\": [" + JSONString + "] }";

        makeCloudEnv(vcapServices);
        String CF_VCAP_SERVICES = VCAP_SVC_ENV + "=" + vcapServices;

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.jms.host=192.168.1.80:55500",
                "solace.jms.clientUsername=bob", "solace.jms.clientPassword=password", "solace.jms.msgVpn=newVpn");

        SpringSolJmsConnectionFactoryCloudFactory springSolConnectionFactoryCloudFactory = context
                .getBean(SpringSolJmsConnectionFactoryCloudFactory.class);
        assertNotNull(springSolConnectionFactoryCloudFactory);

        SolConnectionFactory solConnectionFactory = context.getBean(SolConnectionFactory.class);
        assertNotNull(solConnectionFactory);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

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