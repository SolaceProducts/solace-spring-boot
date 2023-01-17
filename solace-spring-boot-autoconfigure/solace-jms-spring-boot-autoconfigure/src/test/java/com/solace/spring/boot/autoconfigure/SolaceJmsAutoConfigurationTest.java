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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jms.core.JmsTemplate;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
public class SolaceJmsAutoConfigurationTest extends SolaceJmsAutoConfigurationTestBase {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    public SolaceJmsAutoConfigurationTest() {
        super(SolaceJmsAutoConfiguration.class);
    }

    @Test
    void defaultNativeConnectionFactory() {
        load("");
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        SolConnectionFactoryImpl connectionFactory = context.getBean(SolConnectionFactoryImpl.class);
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
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        SolConnectionFactoryImpl connectionFactory = context.getBean(SolConnectionFactoryImpl.class);
        assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
        assertEquals("tcp://192.168.1.80:55500", connectionFactory.getHost());
        assertEquals("newVpn", connectionFactory.getVPN());
        assertEquals("bob", connectionFactory.getUsername());
        assertEquals("password", connectionFactory.getPassword());
        assertFalse(connectionFactory.getDirectTransport());
    }

    @Test
    void externallyLoadedServiceProperties() {
        // Testing one type of externally loaded service is good enough
        // The loader has its own tests for the other scenarios
        String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";

        EnvConfig configuration = getOneSolaceService(ENV_SOLCAP_SERVICES);
        environmentVariables.set(configuration.envName(), configuration.envValue());
        load(String.format("%s=%s", ENV_SOLCAP_SERVICES, configuration.envValue()));

        String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
        assertNotNull(solaceManifest);
        assertTrue(solaceManifest.contains("solace-pubsub"));

        assertNotNull(context.getBean(SolConnectionFactory.class));
        assertNotNull(context.getBean(SpringSolJmsConnectionFactoryCloudFactory.class));
        assertNotNull(context.getBean(SolaceServiceCredentials.class));
    }

    @Test
    void noExternallyLoadedServiceProperties() {
        // Testing one type of externally loaded service is good enough
        // The loader has its own tests for the other scenarios
        String ENV_SOLCAP_SERVICES = "SOLCAP_SERVICES";
        load(String.format("%s={ \"solace-pubsub\": [] }", ENV_SOLCAP_SERVICES));

        String solaceManifest = context.getEnvironment().getProperty(ENV_SOLCAP_SERVICES);
        assertNotNull(solaceManifest);
        assertTrue(solaceManifest.contains("solace-pubsub"));

        assertNotNull(context.getBean(SolConnectionFactory.class));
        assertNotNull(context.getBean(SpringSolJmsConnectionFactoryCloudFactory.class));
        NoSuchBeanDefinitionException thrown = assertThrows(NoSuchBeanDefinitionException.class, () ->
                context.getBean(SolaceServiceCredentials.class));
        assertEquals(SolaceServiceCredentials.class, thrown.getBeanType());
    }
}
