/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.spring.boot.autoconfigure.workaround;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJndi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.util.StringUtils;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;
import java.util.Arrays;

/**
 * <p>This is a Spring Boot 3 patch when downgrading a Spring Boot 3 application to use javax JMS libraries.
 * <p>Original source from https://github.com/spring-projects/spring-boot/tree/v2.7.7 with some modifications.
 * <p>This patch is only intended for temporary use.
 * <p>
 * <p>Copyright (c) 2012-2023 VMware, Inc.
 * <p>https://github.com/spring-projects/spring-boot/blob/v2.7.7/buildSrc/src/main/resources/NOTICE.txt
 *
 * <hr>
 *
 * <p>{@link EnableAutoConfiguration Auto-configuration} for JMS provided from JNDI.
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
@AutoConfiguration(before = {
		com.solace.spring.boot.autoconfigure.workaround.JmsAutoConfiguration.class,
		org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration.class,
		org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration.class})
@ConditionalOnClass(JmsTemplate.class)
@ConditionalOnMissingBean(ConnectionFactory.class)
@Conditional({SpringBoot3Condition.class, JndiConnectionFactoryAutoConfiguration.JndiOrPropertyCondition.class})
@EnableConfigurationProperties(JmsProperties.class)
public class JndiConnectionFactoryAutoConfiguration {

	// Keep these in sync with the condition below
	private static final String[] JNDI_LOCATIONS = { "java:/JmsXA", "java:/XAConnectionFactory" };

	@Bean
	public ConnectionFactory jmsConnectionFactory(JmsProperties properties) throws NamingException {
		JndiLocatorDelegate jndiLocatorDelegate = JndiLocatorDelegate.createDefaultResourceRefLocator();
		if (StringUtils.hasLength(properties.getJndiName())) {
			return jndiLocatorDelegate.lookup(properties.getJndiName(), ConnectionFactory.class);
		}
		return findJndiConnectionFactory(jndiLocatorDelegate);
	}

	private ConnectionFactory findJndiConnectionFactory(JndiLocatorDelegate jndiLocatorDelegate) {
		for (String name : JNDI_LOCATIONS) {
			try {
				return jndiLocatorDelegate.lookup(name, ConnectionFactory.class);
			}
			catch (NamingException ex) {
				// Swallow and continue
			}
		}
		throw new IllegalStateException(
				"Unable to find ConnectionFactory in JNDI locations " + Arrays.asList(JNDI_LOCATIONS));
	}

	/**
	 * Condition for JNDI name or a specific property.
	 */
	static class JndiOrPropertyCondition extends AnyNestedCondition {

		JndiOrPropertyCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnJndi({ "java:/JmsXA", "java:/XAConnectionFactory" })
		static class Jndi {

		}

		@ConditionalOnProperty(prefix = "spring.jms", name = "jndi-name")
		static class Property {

		}

	}

}
