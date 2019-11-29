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

package com.solace.spring.cloud.cloudfoundry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.*;

import com.google.gson.Gson;
import com.solace.services.core.model.SolaceServiceCredentials;
import org.junit.Rule;
import org.junit.Test;

import com.solace.spring.cloud.core.SolaceServiceCredentialsFactory;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;

public class SolaceServiceCredentialsFactoryTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	private static final String PUBSUB_LABEL = "solace-pubsub";
	private static final String PUBSUB_TAG = "solace-pubsub";

	private static final String USER_PROVIDED_LABEL = "user-provided";
	private static final String USER_PROVIDED_NAME = "user-provided";

	@BeforeEach
    public void cleanEnvironment() {
        environmentVariables.clear("VCAP_SERVICES");
    }

    @Test
	public void testNoServices() {
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(0, solaceServiceCredentialsList.size());
	}

	@Test
	public void testSinglePubsubServiceWithLabelAndTag() {
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("test-solace-pubsub", PUBSUB_LABEL, tags));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
		validateSolaceServiceCredentials(solaceServiceCredentialsList.get(0));
	}

	@Test
	public void testSinglePubsubServiceWithLabelOnly() {
		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("test-solace-pubsub", PUBSUB_LABEL, null));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
		validateSolaceServiceCredentials(solaceServiceCredentialsList.get(0));
	}

	@Test
	public void testSinglePubsubServiceWithTagOnly() {

		// This label won't be used for solace pubsub service discovery
		// The specified tag 'solace-pubsub' will be used instead
		String label = "i-am-still-a-solace-pubsub-service";
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("test-solace-pubsub", label, tags));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
		validateSolaceServiceCredentials(solaceServiceCredentialsList.get(0));
	}

	@Test
	public void testMultiplePubsubServicesWithLabelAndTag() {
		String label = PUBSUB_LABEL;
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-1", label, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-2", label, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-3", label, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-4", label, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-5", label, tags));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(5, solaceServiceCredentialsList.size());
		for (SolaceServiceCredentials solaceServiceCredentials : solaceServiceCredentialsList) {
			validateSolaceServiceCredentials(solaceServiceCredentials);
		}
	}

	@Test
	public void testMultiplePubsubServicesWithMixedLabelsAndTags() {
		String label = PUBSUB_LABEL;
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// This label won't be used for solace pubsub service discovery
		// The specified tag 'solace-pubsub' will be used instead
		String alternateLabel = "i-am-still-a-solace-pubsub-service";

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-1", label, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-2", alternateLabel, tags));
		pubsubServices.add(createSolacePubsubVcapService(USER_PROVIDED_NAME, USER_PROVIDED_LABEL, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-4", alternateLabel, tags));
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-5", label, null));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(5, solaceServiceCredentialsList.size());
		for (SolaceServiceCredentials solaceServiceCredentials : solaceServiceCredentialsList) {
			validateSolaceServiceCredentials(solaceServiceCredentials);
		}
	}

	@Test
	public void testOnlyThirdPartyServices() {
		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createThirdPartyVcapService("rabbit-mq-1", "rabbit-mq", Arrays.asList("pivotal", "rabbit-mq")));
		pubsubServices.add(createThirdPartyVcapService("MySQL DB", "MySQL", Arrays.asList("sql", "mysql", "database", "relational")));
		pubsubServices.add(createThirdPartyVcapService("Kakfa instance", "Kafka", Arrays.asList("apache", "kafka")));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(0, solaceServiceCredentialsList.size());
	}

	@Test
	public void testSinglePubsubServiceWithThirdPartyServices() {
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("solace-pubsub-1", PUBSUB_LABEL, tags));
		pubsubServices.add(createThirdPartyVcapService("rabbit-mq-1", "rabbit-mq", Arrays.asList("pivotal", "rabbit-mq")));
		pubsubServices.add(createThirdPartyVcapService("MySQL DB", "MySQL", Arrays.asList("sql", "mysql", "database", "relational")));
		pubsubServices.add(createThirdPartyVcapService("Kakfa instance", "Kafka", Arrays.asList("apache", "kafka")));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
		validateSolaceServiceCredentials(solaceServiceCredentialsList.get(0));
	}

	// We could do a lot more negative testing. But other Spring cloud
	// connectors seem very tolerant. For now starting with this limited
	// coverage.
	@Test
	public void testSinglePubsubServiceWithMissingCredentials() {
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("test-solace-pubsub", PUBSUB_LABEL, tags));
		pubsubServices.get(0).remove("credentials");
		setVcapServices(createVcapServiceList(pubsubServices));

		// Should still be accepted
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSinglePubsubServiceWithCorruptedCredentials() {
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService("test-solace-pubsub", PUBSUB_LABEL, tags));
		((Map<String,Object>) pubsubServices.get(0).get("credentials")).remove("smfHosts");
		setVcapServices(createVcapServiceList(pubsubServices));

		// Should still be accepted
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());

		// Validate smf is null, while other credentials exist
		SolaceServiceCredentials credentials = solaceServiceCredentialsList.get(0);
		assertNull(credentials.getSmfHost());
		assertEquals("tcps://192.168.1.50:7003,tcps://192.168.1.51:7003", credentials.getSmfTlsHost());
	}

	@Test
	public void testSingleUserProvidedPubsubService() {
		List<String> tags = Collections.singletonList(PUBSUB_TAG);

		// Create services
		List<Map<String, Object>> pubsubServices = new ArrayList<>();
		pubsubServices.add(createSolacePubsubVcapService(USER_PROVIDED_NAME, USER_PROVIDED_LABEL, tags));
		setVcapServices(createVcapServiceList(pubsubServices));

		// Validate services credentials were loaded correctly
		List<SolaceServiceCredentials> solaceServiceCredentialsList = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
		assertEquals(1, solaceServiceCredentialsList.size());
		validateSolaceServiceCredentials(solaceServiceCredentialsList.get(0));
	}

    private void setVcapServices(Map<String, List<Object>> vcapServicesData) {
        Gson gson = new Gson();
        String vcapServicesJson = gson.toJson(vcapServicesData);
        System.out.println("VCAP_SERVICES = " + vcapServicesJson);
        environmentVariables.set("VCAP_SERVICES", vcapServicesJson);
    }

	/**
	 * @return a representation of the solace service credentials of a solace VCAP service
	 */
	private Map<String,Object> createServiceCredentials() {

		Map<String, Object> credentials = new HashMap<>();

		credentials.put("clientUsername", "sample-client-username");
		credentials.put("clientPassword", "sample-client-password");
		credentials.put("msgVpnName", "sample-msg-vpn");
		credentials.put("smfHosts", Collections.singletonList("tcp://192.168.1.50:7000"));
		credentials.put("smfTlsHosts", Arrays.asList("tcps://192.168.1.50:7003", "tcps://192.168.1.51:7003"));
		credentials.put("smfZipHosts", Collections.singletonList("tcp://192.168.1.50:7001"));
		credentials.put("webMessagingUris", Collections.singletonList("http://192.168.1.50:80"));
		credentials.put("webMessagingTlsUris", Collections.singletonList("https://192.168.1.50:80"));
		credentials.put("jmsJndiUris", Collections.singletonList("smf://192.168.1.50:7000"));
		credentials.put("jmsJndiTlsUris", Arrays.asList("smfs://192.168.1.50:7003", "smfs://192.168.1.51:7003"));
		credentials.put("mqttUris", Collections.singletonList("tcp://192.168.1.50:7020"));
		credentials.put("mqttTlsUris", Arrays.asList("ssl://192.168.1.50:7021", "ssl://192.168.1.51:7021"));
		credentials.put("mqttWsUris", Collections.singletonList("ws://192.168.1.50:7022"));
		credentials.put("mqttWssUris", Arrays.asList("wss://192.168.1.50:7023", "wss://192.168.1.51:7023"));
		credentials.put("restUris", Collections.singletonList("http://192.168.1.50:7018"));
		credentials.put("restTlsUris", Collections.singletonList("https://192.168.1.50:7019"));
		credentials.put("amqpUris", Collections.singletonList("amqp://192.168.1.50:7016"));
		credentials.put("amqpTlsUris", Collections.singletonList("amqps://192.168.1.50:7017"));
		credentials.put("managementHostnames", Collections.singletonList("vmr-Medium-VMR-0"));
		credentials.put("managementUsername", "sample-mgmt-username");
		credentials.put("managementPassword", "sample-mgmt-password");
		credentials.put("activeManagementHostname", "vmr-medium-web");
		credentials.put("dmrClusterName", "dmr-cluster-name");
		credentials.put("dmrClusterPassword", "dmr-cluster-password");

		return credentials;
	}

	/**
	 * @return a map representation of a single VCAP service listing
	 * i.e.:
	 * {
	 *     "binding_name": null,
	 *     "credentials": { (credentials) },
	 *     "instance_name": "cool-pubsub-service",
	 *     "label": "solace-pubsub",
	 *     "name": "cool-pubsub-service",
	 *     "plan": "standard-plan-3",
	 *     "tags": [ (tags) ],
	 * }
	 */
	private Map<String, Object> createSolacePubsubVcapService(String name, String label, List<String> tags) {
		Map<String, Object> vcapService = new HashMap<>();
		vcapService.put("credentials", createServiceCredentials());
		vcapService.put("label", label);
		vcapService.put("name", name);
		vcapService.put("plan", "vmr-shared");
		vcapService.put("provider", "Solace Systems");
		vcapService.put("tags", tags);

		return vcapService;
	}

	private Map<String, Object> createThirdPartyVcapService(String name, String label, List<String> tags) {
		Map<String, Object> vcapService = new HashMap<>();
		vcapService.put("credentials", null);
		vcapService.put("label", label);
		vcapService.put("name", name);
		vcapService.put("plan", "dummy-plan");
		vcapService.put("provider", "Company Name");
		vcapService.put("tags", tags);

		return vcapService;
	}

	/**
	 * @return a map representation of the VCAP service listings
	 * This is what would be found at the root of the VCAP_SERVICES environment variable
	 * i.e.:
	 * "solace-pubsub": [
	 *    { (service listing) },
	 *    { (service listing) }
	 *  ],
	 * "user-provided": [
	 *    { (service listing) }
	 *  ]
	 */
	private Map<String,List<Object>> createVcapServiceList(List<Map<String, Object>> vcapServices) {
		Map<String, List<Object>> vcapServiceList = new HashMap<>();

		// Add services to VCAP service list by label
		for (Map<String,Object> service : vcapServices) {
			String label = (String) service.get("label");

			if (label != null) {
				if (vcapServiceList.get(label) == null) {
					vcapServiceList.computeIfAbsent(label, value -> new ArrayList<>());
					vcapServiceList.put(label, new ArrayList<>());
				}
				vcapServiceList.get(label).add(service);
			}
		}

		return vcapServiceList;
	}

	private void validateSolaceServiceCredentials(SolaceServiceCredentials credentials) {
		// Validate it all got set correctly.

		// Check Top Level stuff
		assertEquals("sample-client-username", credentials.getClientUsername());
		assertEquals("sample-client-password", credentials.getClientPassword());
		assertEquals("sample-msg-vpn", credentials.getMsgVpnName());

		// Check SMF
		assertEquals("tcp://192.168.1.50:7000", credentials.getSmfHost());
		assertEquals("tcps://192.168.1.50:7003,tcps://192.168.1.51:7003", credentials.getSmfTlsHost());
		assertEquals("tcp://192.168.1.50:7001", credentials.getSmfZipHost());

		// Check JMS
		assertEquals("smf://192.168.1.50:7000", credentials.getJmsJndiUri());
		assertEquals("smfs://192.168.1.50:7003,smfs://192.168.1.51:7003", credentials.getJmsJndiTlsUri());

		// Check MQTT
		assertThat(credentials.getMqttUris(), is(Collections.singletonList("tcp://192.168.1.50:7020")));
		assertThat(credentials.getMqttTlsUris(), is(Arrays.asList("ssl://192.168.1.50:7021", "ssl://192.168.1.51:7021")));
		assertThat(credentials.getMqttWsUris(), is(Collections.singletonList("ws://192.168.1.50:7022")));
		assertThat(credentials.getMqttWssUris(), is(Arrays.asList("wss://192.168.1.50:7023", "wss://192.168.1.51:7023")));

		// Check REST
		assertThat(credentials.getRestUris(), is(Collections.singletonList("http://192.168.1.50:7018")));
		assertThat(credentials.getRestTlsUris(), is(Collections.singletonList("https://192.168.1.50:7019")));

		// Check AMQP
		assertThat(credentials.getAmqpUris(), is(Collections.singletonList("amqp://192.168.1.50:7016")));
		assertThat(credentials.getAmqpTlsUris(), is(Collections.singletonList("amqps://192.168.1.50:7017")));

		// Check Management Interfaces
		assertThat(credentials.getManagementHostnames(), is(Collections.singletonList("vmr-Medium-VMR-0")));
		assertEquals("sample-mgmt-username", credentials.getManagementUsername());
		assertEquals("sample-mgmt-password", credentials.getManagementPassword());
		assertEquals("vmr-medium-web", credentials.getActiveManagementHostname());

		// Check DMR Clusters
		assertEquals("dmr-cluster-name", credentials.getDmrClusterName());
		assertEquals("dmr-cluster-password", credentials.getDmrClusterPassword());
	}
}
