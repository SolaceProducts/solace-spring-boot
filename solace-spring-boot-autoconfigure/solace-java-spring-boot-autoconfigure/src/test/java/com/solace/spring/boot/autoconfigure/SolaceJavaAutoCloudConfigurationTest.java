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
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
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
public class SolaceJavaAutoCloudConfigurationTest<T> extends SolaceJavaAutoConfigurationTestBase {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private static final String VCAP_SVC_ENV = "VCAP_SERVICES";
    private static final String VCAP_APP_ENV = "VCAP_APPLICATION";
    // Just enough to satisfy the Cloud Condition we need
    private static final String CF_CLOUD_APP_ENV = VCAP_APP_ENV + "={}";

    // Some other Service
    private static final String CF_VCAP_SERVICES_OTHER = VCAP_SVC_ENV + "={ otherService: [ { id: '1' } , { id: '2' } ]}";

    public SolaceJavaAutoCloudConfigurationTest() {
        super(SolaceJavaAutoCloudConfiguration.class);
    }

    public static Collection<Object[]> parameterData() {
        Set<ResolvableType> classes = new HashSet<>();
        classes.add(ResolvableType.forClass(SpringJCSMPFactoryCloudFactory.class));
        classes.add(ResolvableType.forClass(SpringJCSMPFactory.class));
        classes.add(ResolvableType.forClass(JCSMPProperties.class));
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
        NoSuchBeanDefinitionException exception = assertThrows(NoSuchBeanDefinitionException.class, () -> this.context.getBean(beanClass));
        assertTrue(exception.getBeanType().isAssignableFrom(beanClass));
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

        T bean = this.context.getBean(beanClass);
        assertNotNull(bean);

        if (beanClass.equals(SpringJCSMPFactoryCloudFactory.class)) {
            SpringJCSMPFactoryCloudFactory springJCSMPFactoryCloudFactory = (SpringJCSMPFactoryCloudFactory) bean;
            assertNotNull(springJCSMPFactoryCloudFactory.getSpringJCSMPFactory());
            List<SolaceServiceCredentials> availableServices = springJCSMPFactoryCloudFactory.getSolaceServiceCredentials();
            assertNotNull(availableServices);
            assertEquals(1, availableServices.size());
        } else if (beanClass.equals(JCSMPProperties.class)) {
            new SpringJCSMPFactory((JCSMPProperties) bean);
        }
    }

    @Test
    void isCloudConfiguredBySolaceMessagingInfoAndDefaultsForOtherProperties() throws Exception {
        EnvConfig configuration = getOneSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

        SpringJCSMPFactoryCloudFactory springJCSMPFactoryCloudFactory = this.context
                .getBean(SpringJCSMPFactoryCloudFactory.class);
        assertNotNull(springJCSMPFactoryCloudFactory);

        SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
        assertNotNull(jcsmpFactory);

        JCSMPSession session = jcsmpFactory.createSession();
        assertNotNull(session);

        // The are cloud provided (SolaceMessagingInfo) properties
        assertEquals("tcp://192.168.1.50:7000", session.getProperty(JCSMPProperties.HOST));
        assertEquals("sample-msg-vpn", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("sample-client-username", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("sample-client-password", session.getProperty(JCSMPProperties.PASSWORD));

        // Other non cloud (SolaceMessagingInfo) provided properties
        assertEquals(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO,
                session.getProperty(JCSMPProperties.MESSAGE_ACK_MODE));
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
    void isCloudConfiguredByUserProvidedServices() throws Exception {
        EnvConfig configuration = getOneUserProvidedSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES);

        SpringJCSMPFactoryCloudFactory springJCSMPFactoryCloudFactory = this.context
                .getBean(SpringJCSMPFactoryCloudFactory.class);
        assertNotNull(springJCSMPFactoryCloudFactory);

        SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
        assertNotNull(jcsmpFactory);

        JCSMPSession session = jcsmpFactory.createSession();
        assertNotNull(session);

        // They are cloud provided (SolaceMessagingInfo) properties
        assertEquals("tcp://192.168.1.51:7000", session.getProperty(JCSMPProperties.HOST));
        assertEquals("sample-msg-vpn2", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("sample-client-username2", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("sample-client-password2", session.getProperty(JCSMPProperties.PASSWORD));

        // Other non cloud (SolaceMessagingInfo) provided properties
        assertEquals(JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO,
                session.getProperty(JCSMPProperties.MESSAGE_ACK_MODE));
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
    void isCloudConfiguredBySolaceMessagingInfoAndOtherProperties() throws Exception {
        EnvConfig configuration = getOneSolaceService(VCAP_SVC_ENV);
        makeCloudEnv(configuration.envValue());
        String CF_VCAP_SERVICES = configuration.envName() + "=" + configuration.envValue();

        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.java.host=192.168.1.80:55500", "solace.java.clientUsername=bob",
                "solace.java.clientPassword=password", "solace.java.msgVpn=newVpn",
                "solace.java.clientName=client-name", "solace.java.connectRetries=5", "solace.java.reconnectRetries=10",
                "solace.java.connectRetriesPerHost=40", "solace.java.reconnectRetryWaitInMillis=1000",
                "solace.java.messageAckMode=client_ack", "solace.java.reapplySubscriptions=true",
                "solace.java.advanced.jcsmp.TOPIC_DISPATCH=true");

        SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
        JCSMPSession session = jcsmpFactory.createSession();

        // The are cloud provided (SolaceMessagingInfo) properties
        assertEquals("tcp://192.168.1.50:7000", session.getProperty(JCSMPProperties.HOST));
        assertEquals("sample-msg-vpn", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("sample-client-username", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("sample-client-password", session.getProperty(JCSMPProperties.PASSWORD));

        // Other non cloud provided properties..
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
    void isCloudConfiguredBySolaceMessagingInfoAndOtherPropertiesWhenMissingCredentials() throws Exception {
        Map<String, Object> services = createOneService();
        @SuppressWarnings("unchecked")
        Map<String, Object> credentials = (Map<String, Object>) services.get("credentials");
        credentials.remove("clientUsername");
        credentials.remove("clientPassword");

        JSONObject jsonMapObject = new JSONObject(services);
        String JSONString = jsonMapObject.toString();
        String vcapService = "{ \"solace-pubsub\": [" + JSONString + "] }";

        makeCloudEnv(vcapService);
        String CF_VCAP_SERVICES = VCAP_SVC_ENV + "=" + vcapService;
        load(CF_CLOUD_APP_ENV, CF_VCAP_SERVICES, "solace.java.host=192.168.1.80:55500", "solace.java.clientUsername=bob",
                "solace.java.clientPassword=password", "solace.java.msgVpn=newVpn",
                "solace.java.clientName=client-name", "solace.java.connectRetries=5", "solace.java.reconnectRetries=10",
                "solace.java.connectRetriesPerHost=40", "solace.java.reconnectRetryWaitInMillis=1000",
                "solace.java.messageAckMode=client_ack", "solace.java.reapplySubscriptions=true",
                "solace.java.advanced.jcsmp.TOPIC_DISPATCH=true");

        SpringJCSMPFactory jcsmpFactory = this.context.getBean(SpringJCSMPFactory.class);
        JCSMPSession session = jcsmpFactory.createSession();

        // The are cloud provided (SolaceMessagingInfo) properties
        assertEquals("tcp://192.168.1.50:7000", session.getProperty(JCSMPProperties.HOST));
        assertEquals("sample-msg-vpn", session.getProperty(JCSMPProperties.VPN_NAME));
        assertEquals("bob", session.getProperty(JCSMPProperties.USERNAME));
        assertEquals("password", session.getProperty(JCSMPProperties.PASSWORD));

        // Other non cloud provided properties..
        assertEquals("client-name", session.getProperty(JCSMPProperties.CLIENT_NAME));
        // Channel properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) session
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        assertEquals(5, cp.getConnectRetries());
        assertEquals(10, cp.getReconnectRetries());
        assertEquals(40, cp.getConnectRetriesPerHost());
        assertEquals(1000, cp.getReconnectRetryWaitInMillis());
    }

    private void makeCloudEnv(String vcapService) {
        environmentVariables.set(VCAP_APP_ENV, "{}");
        assertEquals("{}", System.getenv(VCAP_APP_ENV));
        environmentVariables.set(VCAP_SVC_ENV, vcapService);
        assertEquals(vcapService, System.getenv(VCAP_SVC_ENV));
    }
}