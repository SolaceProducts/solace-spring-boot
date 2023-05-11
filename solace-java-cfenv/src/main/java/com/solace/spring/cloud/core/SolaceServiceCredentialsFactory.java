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
package com.solace.spring.cloud.core;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides easy access to all of the information in the VCAP_SERVICES environment variable without
 * extra dependencies on any Solace Enterprise APIs.
 *
 * For more details see the GitHub project:
 *    - https://github.com/SolaceProducts/sl-spring-cloud-connectors
 *
 */
@SuppressWarnings("unused")
public class SolaceServiceCredentialsFactory {

	private static final String solacePubSubLabel = "solace-pubsub";
	private static final String solacePubSubTag = "solace-pubsub";

	/**
	 * Loads the service credentials from all detected Solace PubSub+ services in the current cloud foundry environment
	 * @return a non-null list of solace service credentials. This list will be empty if no PubSub+ services are detected
	 */
	public static List<SolaceServiceCredentials> getAllFromCloudFoundry() {
		CfEnv cfEnv = new CfEnv();

		List<CfService> services = new ArrayList<>(cfEnv.findServicesByLabel(solacePubSubLabel));

		// Some services have both the pubsub label and tag, ensure we do not have duplicates
		for (CfService serviceA : cfEnv.findServicesByTag(solacePubSubTag)) {
			boolean unique = true;
			for (CfService serviceB : services) {
				if (serviceA.getName().equals(serviceB.getName())) {
					unique = false;
				}
			}
			if (unique) services.add(serviceA);
		}

		List<SolaceServiceCredentials> credentials = new ArrayList<>();
		for (CfService service : services) {
			credentials.add(loadServiceCredentials(service));
		}

		return credentials;
	}

	@SuppressWarnings("unchecked")
	private static SolaceServiceCredentials loadServiceCredentials(CfService service) {
		SolaceServiceCredentialsImpl credentials = new SolaceServiceCredentialsImpl();
		credentials.setId(service.getName());

		// Populate this the quick and dirty way for now. Can improve later as
		// we harden. As a start, we'll be tolerant of missing attributes and
		// just leave fields set to null.
		for (Map.Entry<String, Object> entry : service.getCredentials().getMap().entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			switch (key) {
				case "clientUsername":
					credentials.setClientUsername((String) value);
					break;
				case "clientPassword":
					credentials.setClientPassword((String) value);
					break;
				case "msgVpnName":
					credentials.setMsgVpnName((String) value);
					break;
				case "smfHosts":
					credentials.setSmfHosts((List<String>) value);
					break;
				case "smfTlsHosts":
					credentials.setSmfTlsHosts((List<String>) value);
					break;
				case "smfZipHosts":
					credentials.setSmfZipHosts((List<String>) value);
					break;
				case "jmsJndiUris":
					credentials.setJmsJndiUris((List<String>) value);
					break;
				case "jmsJndiTlsUris":
					credentials.setJmsJndiTlsUris((List<String>) value);
					break;
				case "managementUsername":
					credentials.setManagementUsername((String) value);
					break;
				case "managementPassword":
					credentials.setManagementPassword((String) value);
					break;
				case "activeManagementHostname":
					credentials.setActiveManagementHostname((String) value);
					break;
				case "restUris":
					credentials.setRestUris((List<String>) value);
					break;
				case "restTlsUris":
					credentials.setRestTlsUris((List<String>) value);
					break;
				case "mqttUris":
					credentials.setMqttUris((List<String>) value);
					break;
				case "mqttTlsUris":
					credentials.setMqttTlsUris((List<String>) value);
					break;
				case "mqttWsUris":
					credentials.setMqttWsUris((List<String>) value);
					break;
				case "mqttWssUris":
					credentials.setMqttWssUris((List<String>) value);
					break;
				case "amqpUris":
					credentials.setAmqpUris((List<String>) value);
					break;
				case "amqpTlsUris":
					credentials.setAmqpTlsUris((List<String>) value);
					break;
				case "managementHostnames":
					credentials.setManagementHostnames((List<String>) value);
					break;
				case "dmrClusterName":
					credentials.setDmrClusterName((String) value);
					break;
				case "dmrClusterPassword":
					credentials.setDmrClusterPassword((String) value);
			}
		}

		return credentials;
	}
}