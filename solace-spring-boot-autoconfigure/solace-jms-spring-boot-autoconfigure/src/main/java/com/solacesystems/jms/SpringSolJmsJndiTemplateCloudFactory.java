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

package com.solacesystems.jms;

import java.util.List;

import com.solace.services.core.model.SolaceServiceCredentials;
import org.springframework.jndi.JndiTemplate;

/**
 * A Factory for {@link JndiTemplate} to Support Cloud Environments having
 * multiple Solace PubSub+ services.
 */
public interface SpringSolJmsJndiTemplateCloudFactory {

	/**
	 * Gets the first detected {@link SolaceServiceCredentials}.
	 *
	 * @return A Solace PubSub+ service
	 */
	SolaceServiceCredentials findFirstSolaceServiceCredentials();

	/**
	 * Lists All Cloud Environment detected Solace PubSub+ services.
	 *
	 * @return List of all Cloud Environment detected Solace PubSub+ services
	 */
	List<SolaceServiceCredentials> getSolaceServiceCredentials();

	/**
	 * Returns a {@link JndiTemplate} based on the first detected {@link SolaceServiceCredentials}.
	 *
	 * @return {@link JndiTemplate} based on the first detected {@link SolaceServiceCredentials}
	 */
	JndiTemplate getJndiTemplate();

	/**
	 * Returns a {@link JndiTemplate} based on the {@link SolaceServiceCredentials}
	 * identified by the given ID.
	 *
	 * @param id The Solace PubSub+ service's ID
	 * @return {@link JndiTemplate} with the given Solace PubSub+ service ID,
	 * otherwise null if the service cannot be found
	 */
	JndiTemplate getJndiTemplate(String id);

	/**
	 * Returns a {@link JndiTemplate} based on the given {@link SolaceServiceCredentials}.
	 *
	 * @param solaceServiceCredentials The credentials to an existing Solace PubSub+ service
	 * @return {@link JndiTemplate} based on the given {@link SolaceServiceCredentials},
	 * otherwise an application.properties based {@link JndiTemplate}
	 */
	JndiTemplate getJndiTemplate(SolaceServiceCredentials solaceServiceCredentials);
}
