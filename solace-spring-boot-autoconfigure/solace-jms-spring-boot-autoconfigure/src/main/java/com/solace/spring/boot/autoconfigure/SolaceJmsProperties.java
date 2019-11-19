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

@ConfigurationProperties("solace.jms")
public class SolaceJmsProperties {

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


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public String getMsgVpn() {
        return msgVpn;
    }

    public void setMsgVpn(String msgVpn) {
        this.msgVpn = msgVpn;
    }

    public boolean isDirectTransport() {
        return directTransport;
    }

    public void setDirectTransport(boolean directTransport) {
        this.directTransport = directTransport;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Map<String,String> getApiProperties() {
        return apiProperties;
    }
}