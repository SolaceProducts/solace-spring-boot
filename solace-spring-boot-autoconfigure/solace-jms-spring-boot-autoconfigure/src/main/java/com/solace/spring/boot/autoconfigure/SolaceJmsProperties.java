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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for Solace JMS API integration.
 * <p>
 * These properties are bound from the {@code solace.jms.*} namespace in application properties
 * and are used to configure the Solace JMS connection factory.
 * </p>
 * @see com.solace.spring.boot.autoconfigure.SolaceJmsAutoConfiguration
 */
@ConfigurationProperties("solace.jms")
public class SolaceJmsProperties {

    /**
     * Creates a new SolaceJmsProperties instance with default values.
     * This constructor is used by Spring Boot's configuration properties binding.
     */
    public SolaceJmsProperties() {
    }

    @NestedConfigurationProperty
    private final Map<String,String> apiProperties = new ConcurrentHashMap<>();

    /**
     * Solace Message Router Host address. Port is optional and intelligently defaulted by the Solace JMS API.
     */
    private String host = "localhost";

    /**
     * Solace Message Router Message-VPN
     */
    private String msgVpn = "default";

    /**
     * Solace Message Router Client Username
     */
    private String clientUsername = "spring-default-client-username";

    /**
     * Solace Message Router Client Password
     */
    private String clientPassword = "";

    /**
     * A flag to control whether or not to enable the Solace direct transport JMS feature. Enabling this feature allows for higher performance but limits the JMS features that are supported.
     */
    private boolean directTransport = false;

    /**
     * The client name to use when connecting to Solace Message Router. This must be unique. If absent, the API will generate a reasonable client name.
     */
    private String clientName;


    /**
     * Gets the Solace Message Router Host address.
     * @return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the Solace Message Router Host address.
     * @param host the host address to use
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the Solace Message Router Client Username.
     * @return the client username
     */
    public String getClientUsername() {
        return clientUsername;
    }

    /**
     * Sets the Solace Message Router Client Username.
     * @param clientUsername the client username to use
     */
    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    /**
     * Gets the Solace Message Router Client Password.
     * @return the client password
     */
    public String getClientPassword() {
        return clientPassword;
    }

    /**
     * Sets the Solace Message Router Client Password.
     * @param clientPassword the client password to use
     */
    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    /**
     * Gets the Solace Message Router Message-VPN.
     * @return the message VPN name
     */
    public String getMsgVpn() {
        return msgVpn;
    }

    /**
     * Sets the Solace Message Router Message-VPN.
     * @param msgVpn the message VPN name to use
     */
    public void setMsgVpn(String msgVpn) {
        this.msgVpn = msgVpn;
    }

    /**
     * Checks whether the Solace direct transport JMS feature is enabled.
     * @return {@code true} if direct transport is enabled, {@code false} otherwise
     */
    public boolean isDirectTransport() {
        return directTransport;
    }

    /**
     * Sets whether to enable the Solace direct transport JMS feature.
     * @param directTransport {@code true} to enable direct transport, {@code false} to disable
     */
    public void setDirectTransport(boolean directTransport) {
        this.directTransport = directTransport;
    }

    /**
     * Gets the client name to use when connecting to Solace Message Router.
     * @return the client name, or {@code null} if the API will generate one
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Sets the client name to use when connecting to Solace Message Router.
     * @param clientName the client name to use
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets the API properties map for additional JMS configuration.
     * @return the API properties map
     */
    public Map<String,String> getApiProperties() {
        return apiProperties;
    }
}