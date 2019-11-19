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

package com.solacesystems.jcsmp;

import java.util.List;

import com.solace.services.core.model.SolaceServiceCredentials;

/**
 * A Factory for {@link SpringJCSMPFactory} to Support Cloud Environments having
 * multiple solace-pubsub services.
 */
public interface SpringJCSMPFactoryCloudFactory {
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
	 * Returns a {@link SpringJCSMPFactory} based on the first detected {@link SolaceServiceCredentials}.
	 *
	 * @return {@link SpringJCSMPFactory} based on the first detected {@link SolaceServiceCredentials}
	 */
	SpringJCSMPFactory getSpringJCSMPFactory();

	/**
	 * Returns a {@link SpringJCSMPFactory} based on the {@link SolaceServiceCredentials}
	 * identified by the given ID.
	 *
	 * @param id The Solace PubSub+ service's ID
	 * @return {@link SpringJCSMPFactory} with the given Solace PubSub+ service ID,
	 * otherwise null if the service cannot be found
	 */
	SpringJCSMPFactory getSpringJCSMPFactory(String id);

	/**
	 * Returns a {@link SpringJCSMPFactory} based on the given {@link SolaceServiceCredentials}.
	 *
	 * @param solaceServiceCredentials The credentials to an existing Solace PubSub+ service
	 * @return {@link SpringJCSMPFactory} based on the given {@link SolaceServiceCredentials},
	 * otherwise an application.properties based {@link SpringJCSMPFactory}
	 */
	SpringJCSMPFactory getSpringJCSMPFactory(SolaceServiceCredentials solaceServiceCredentials);

	/**
	 * Returns a {@link JCSMPProperties} based on the first detected {@link SolaceServiceCredentials}.
	 *
	 * @return {@link JCSMPProperties} based on the first detected {@link SolaceServiceCredentials}
	 */
	JCSMPProperties getJCSMPProperties();

	/**
	 * Returns a {@link JCSMPProperties} based on the {@link SolaceServiceCredentials}
	 * identified by the given ID.
	 *
	 * @param id The Solace PubSub+ service's ID
	 * @return {@link JCSMPProperties} with the given Solace PubSub+ service ID,
	 * otherwise null if the service cannot be found
	 */
	JCSMPProperties getJCSMPProperties(String id);

	/**
	 * Returns a {@link JCSMPProperties} based on the given {@link SolaceServiceCredentials}.
	 *
	 * @param solaceServiceCredentials The credentials to an existing Solace PubSub+ service
	 * @return {@link JCSMPProperties} based on the given {@link SolaceServiceCredentials},
	 * otherwise an application.properties based {@link JCSMPProperties}
	 */
	JCSMPProperties getJCSMPProperties(SolaceServiceCredentials solaceServiceCredentials);
}
