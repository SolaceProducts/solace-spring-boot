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
import com.solace.spring.cloud.core.SolaceServiceCredentialsFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureBefore(SolaceJavaAutoConfiguration.class)
@ConditionalOnClass(JCSMPProperties.class)
@ConditionalOnMissingBean(SpringJCSMPFactory.class)
@EnableConfigurationProperties(SolaceJavaProperties.class)
@Conditional(CloudCondition.class)
public class SolaceJavaAutoCloudConfiguration extends SolaceJavaAutoConfigurationBase {

    @Autowired
    public SolaceJavaAutoCloudConfiguration(SolaceJavaProperties properties) {
        super(properties);
    }

    @Override
    public List<SolaceServiceCredentials> getSolaceServiceCredentials() {
        return SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
    }

    @Override
    SolaceServiceCredentials findFirstSolaceServiceCredentialsImpl() {
        List<SolaceServiceCredentials> credentials = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
        return credentials.isEmpty() ? null : credentials.get(0);
    }
}
