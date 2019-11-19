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

import javax.jms.ConnectionFactory;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.spring.cloud.core.SolaceServiceCredentialsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import com.solacesystems.jms.SolConnectionFactory;
import org.springframework.jndi.JndiTemplate;

import java.util.List;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ ConnectionFactory.class, SolConnectionFactory.class })
@ConditionalOnMissingBean({ ConnectionFactory.class, JndiTemplate.class })
@Conditional(CloudCondition.class)
@EnableConfigurationProperties(SolaceJmsProperties.class)
public class SolaceJmsAutoCloudConfiguration extends SolaceJmsAutoConfigurationBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoCloudConfiguration.class);

	@Autowired
	public SolaceJmsAutoCloudConfiguration(SolaceJmsProperties properties) {
		super(properties);
	}

	@Override
	SolaceServiceCredentials findFirstSolaceServiceCredentialsImpl() {
		List<SolaceServiceCredentials> credentials = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		return credentials.size() == 0 ? null : credentials.get(0);
	}

	@Override
	public List<SolaceServiceCredentials> getSolaceServiceCredentials() {
		return SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
	}
}