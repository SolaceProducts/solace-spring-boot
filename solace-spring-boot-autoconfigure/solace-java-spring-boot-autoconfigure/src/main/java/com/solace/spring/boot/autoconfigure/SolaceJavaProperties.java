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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration properties for Solace Java(JCSMP) API integration.
 * <p>
 * These properties are bound from the {@code solace.java.*} namespace in application properties
 * and are used to configure the Solace JCSMP session.
 * </p>
 * @see com.solace.spring.boot.autoconfigure.SolaceJavaAutoConfiguration
 */
@ConfigurationProperties("solace.java")
public class SolaceJavaProperties {

    /**
     * Property key for storing the Spring OAuth2 client registration ID in JCSMP properties.
     * This is used internally to pass the registration ID through to the OAuth2 token provider.
     */
    public static final String SPRING_OAUTH2_CLIENT_REGISTRATION_ID = "SPRING_OAUTH2_CLIENT_REGISTRATION_ID";

    /**
     * Solace Message Router Host address. Port is optional and intelligently defaulted by the Solace Java API.
     */
    private String host = "localhost";

    /**
     * Solace Message Router Message-VPN.
     */
    private String msgVpn = "default";

    /**
     * Solace Message Router Client Username.
     * This field is always used in non cloud deployments.
     * Under a cloud deployment this field is used when the cloud expected value was not provided (null).
     */
    private String clientUsername = "spring-default-client-username";

    /**
     * Solace Message Router Client Password.
     * This field is always used in non cloud deployments.
     * Under a cloud deployment this field is used when the cloud expected value was not provided (null).
     */
    private String clientPassword;

    /**
     * The client name to use when connecting to Solace Message Router. This must be unique. If absent, the API will generate a reasonable client name.
     */
    private String clientName;

    /**
     * The number of times to attempt and retry a connection to the host Solace Message Router (or list of routers) during initial connection setup.
     *
     * This property is optional and defaults to the suggested value when using HA redundant router pairs as documented in the Solace PubSub+ API Developer Guide.
     */
    private int connectRetries = 1;

    /**
     * The number of times to attempt to reconnect to the host Solace Message Router (or list of routers) after an initial connected session goes down.
     *
     * This property is optional and defaults to the suggested value when using HA redundant router pairs as documented in the Solace PubSub+ API Developer Guide.
     */
    private int reconnectRetries = -1;

    /**
     * When using a host list for the HOST property, this property defines how many times to try
     * to connect or reconnect to a single host before moving to the next host in the list.
     * NOTE: This property works in conjunction with the connect and reconnect retries settings;
     * it does not replace them.
     *
     * This property is optional and defaults to the suggested value when using HA redundant router pairs as documented in the Solace PubSub+ API Developer Guide.
     */
    private int connectRetriesPerHost = 20;

    /**
     * How much time in (MS) to wait between each attempt to connect or reconnect to a host.
     * If a connect or reconnect attempt to host is not successful, the API waits for the
     * amount of time set for reconnectRetryWaitInMillis, and then makes another connect or
     * reconnect attempt.
     *
     * This property is optional and defaults to the suggested value when using HA redundant router pairs as documented in the Solace PubSub+ API Developer Guide.
     */
    private int reconnectRetryWaitInMillis = 3000;


    /**
     * API properties can be set by the attribute naming convention used in
     * fromProperties() and toProperties()
     *
     * Example: solace.java.apiProperties.reapply_subscriptions=true
     */
    @NestedConfigurationProperty
    private final Map<String,String> apiProperties = new ConcurrentHashMap<>();

    /**
     * The Spring Security OAuth2 Client Registration Id
     * <code>spring.security.oauth2.client.registration.&lt;registration-id&gt;</code> to use for OAuth2
     * token
     * retrieval. This field is required when the Solace session is configured to use OAuth2 via
     * <code>solace.java.apiProperties.authentication_scheme=AUTHENTICATION_SCHEME_OAUTH2</code>
     */
    private String oauth2ClientRegistrationId;

    /**
     * Gets the Spring Security OAuth2 Client Registration Id.
     * @return the OAuth2 client registration ID, or {@code null} if not configured
     */
    public String getOauth2ClientRegistrationId() {
        return oauth2ClientRegistrationId;
    }

    /**
     * Sets the Spring Security OAuth2 Client Registration Id.
     * @param oauth2ClientRegistrationId the OAuth2 client registration ID to use
     */
    public void setOauth2ClientRegistrationId(String oauth2ClientRegistrationId) {
        this.oauth2ClientRegistrationId = oauth2ClientRegistrationId;
    }

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
     * @return the client password, or {@code null} if not set
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
     * Gets the number of times to attempt and retry a connection during initial connection setup.
     * @return the number of connect retries
     */
    public int getConnectRetries() {
        return connectRetries;
    }

    /**
     * Sets the number of times to attempt and retry a connection during initial connection setup.
     * @param connectRetries the number of connect retries
     */
    public void setConnectRetries(int connectRetries) {
        this.connectRetries = connectRetries;
    }

    /**
     * Gets the number of times to attempt to reconnect after an initial connected session goes down.
     * @return the number of reconnect retries
     */
    public int getReconnectRetries() {
        return reconnectRetries;
    }

    /**
     * Sets the number of times to attempt to reconnect after an initial connected session goes down.
     * @param reconnectRetries the number of reconnect retries
     */
    public void setReconnectRetries(int reconnectRetries) {
        this.reconnectRetries = reconnectRetries;
    }

    /**
     * Gets the number of times to try to connect or reconnect to a single host before moving to the next host.
     * @return the number of connect retries per host
     */
    public int getConnectRetriesPerHost() {
        return connectRetriesPerHost;
    }

    /**
     * Sets the number of times to try to connect or reconnect to a single host before moving to the next host.
     * @param connectRetriesPerHost the number of connect retries per host
     */
    public void setConnectRetriesPerHost(int connectRetriesPerHost) {
        this.connectRetriesPerHost = connectRetriesPerHost;
    }

    /**
     * Gets the time in milliseconds to wait between each attempt to connect or reconnect to a host.
     * @return the reconnect retry wait time in milliseconds
     */
    public int getReconnectRetryWaitInMillis() {
        return reconnectRetryWaitInMillis;
    }

    /**
     * Sets the time in milliseconds to wait between each attempt to connect or reconnect to a host.
     * @param reconnectRetryWaitInMillis the reconnect retry wait time in milliseconds
     */
    public void setReconnectRetryWaitInMillis(int reconnectRetryWaitInMillis) {
        this.reconnectRetryWaitInMillis = reconnectRetryWaitInMillis;
    }

    /**
     * Gets the API properties map for additional JCSMP configuration.
     * @return the API properties map
     */
    public Map<String,String> getApiProperties() {
        return apiProperties;
    }

}