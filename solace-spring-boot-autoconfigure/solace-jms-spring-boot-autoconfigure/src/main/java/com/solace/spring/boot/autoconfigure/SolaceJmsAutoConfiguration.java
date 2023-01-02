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

import com.solace.services.core.loader.SolaceCredentialsLoader;
import com.solace.services.core.model.SolaceServiceCredentials;
import com.solacesystems.jms.SolConnectionFactory;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter(SolaceJmsAutoCloudConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, SolConnectionFactory.class})
@ConditionalOnMissingBean({ConnectionFactory.class, JndiTemplate.class})
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJmsAutoConfiguration extends SolaceJmsAutoConfigurationBase {

    private final SolaceCredentialsLoader solaceServicesInfoLoader = new SolaceCredentialsLoader();

    @Autowired
    public SolaceJmsAutoConfiguration(SolaceJmsProperties properties) {
        super(properties);
    }

    @Override
    SolaceServiceCredentials findFirstSolaceServiceCredentialsImpl() {
        return solaceServicesInfoLoader.getSolaceServiceInfo();
    }

    @Override
    public List<SolaceServiceCredentials> getSolaceServiceCredentials() {
        return new ArrayList<>(solaceServicesInfoLoader.getAllSolaceServiceInfo().values());
    }
}