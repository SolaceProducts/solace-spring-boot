package com.solace.spring.boot.autoconfigure;

import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

public abstract class SolaceJavaAutoConfigurationTestBase {
    AnnotationConfigApplicationContext context;
    private final Class<? extends SpringJCSMPFactoryCloudFactory> configClass;

    @Configuration
    public static class EmptyConfiguration {
    }

    SolaceJavaAutoConfigurationTestBase(Class<? extends SpringJCSMPFactoryCloudFactory> configClass) {
        this.configClass = configClass;
    }

    @AfterEach
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    Map<String, Object> createOneService() {
        Map<String, Object> exVcapServices = new HashMap<>();

        Map<String, Object> exCred = new HashMap<>();

        exCred.put("clientUsername", "sample-client-username");
        exCred.put("clientPassword", "sample-client-password");
        exCred.put("msgVpnName", "sample-msg-vpn");
        exCred.put("smfHosts", singletonList("tcp://192.168.1.50:7000"));
        exCred.put("smfTlsHosts", Arrays.asList("tcps://192.168.1.50:7003", "tcps://192.168.1.51:7003"));
        exCred.put("smfZipHosts", singletonList("tcp://192.168.1.50:7001"));
        exCred.put("webMessagingUris", singletonList("http://192.168.1.50:80"));
        exCred.put("webMessagingTlsUris", singletonList("https://192.168.1.50:80"));
        exCred.put("jmsJndiUris", singletonList("smf://192.168.1.50:7000"));
        exCred.put("jmsJndiTlsUris", Arrays.asList("smfs://192.168.1.50:7003", "smfs://192.168.1.51:7003"));
        exCred.put("mqttUris", singletonList("tcp://192.168.1.50:7020"));
        exCred.put("mqttTlsUris", Arrays.asList("ssl://192.168.1.50:7021", "ssl://192.168.1.51:7021"));
        exCred.put("mqttWsUris", singletonList("ws://192.168.1.50:7022"));
        exCred.put("mqttWssUris", Arrays.asList("wss://192.168.1.50:7023", "wss://192.168.1.51:7023"));
        exCred.put("restUris", singletonList("http://192.168.1.50:7018"));
        exCred.put("restTlsUris", singletonList("https://192.168.1.50:7019"));
        exCred.put("amqpUris", singletonList("amqp://192.168.1.50:7016"));
        exCred.put("amqpTlsUris", singletonList("amqps://192.168.1.50:7017"));
        exCred.put("managementHostnames", singletonList("vmr-Medium-VMR-0"));
        exCred.put("managementUsername", "sample-mgmt-username");
        exCred.put("managementPassword", "sample-mgmt-password");
        exCred.put("activeManagementHostname", "vmr-medium-web");

        exVcapServices.put("credentials", exCred);
        exVcapServices.put("label", "solace-pubsub");
        exVcapServices.put("name", "test-service-instance-name");
        exVcapServices.put("plan", "vmr-shared");
        exVcapServices.put("provider", "Solace Systems");
        // no need to check for tags in terms of validation. It's more for
        exVcapServices.put("tags",
                Arrays.asList("solace", "solace-pubsub", "rest", "mqtt", "mq", "queue", "jms", "messaging", "amqp"));

        return exVcapServices;
    }

    EnvConfig getOneSolaceService(String envName) {
        Map<String, Object> services = createOneService();
        JSONObject jsonMapObject = new JSONObject(services);
        String JSONString = jsonMapObject.toString();
        String configuration = "{ \"solace-pubsub\": [" + JSONString + "] }";
        return new EnvConfig(envName, configuration);
    }

    Map<String, Object> createOneUserProvidedService() {
        Map<String, Object> exVcapServices = new HashMap<>();

        Map<String, Object> exCred = new HashMap<>();

        exCred.put("clientUsername", "sample-client-username2");
        exCred.put("clientPassword", "sample-client-password2");
        exCred.put("msgVpnName", "sample-msg-vpn2");
        exCred.put("smfHosts", singletonList("tcp://192.168.1.51:7000"));
        exCred.put("smfTlsHosts", Arrays.asList("tcps://192.168.1.51:7003", "tcps://192.168.1.51:7003"));
        exCred.put("smfZipHosts", singletonList("tcp://192.168.1.51:7001"));
        exCred.put("webMessagingUris", singletonList("http://192.168.1.51:80"));
        exCred.put("webMessagingTlsUris", singletonList("https://192.168.1.51:80"));
        exCred.put("jmsJndiUris", singletonList("smf://192.168.1.51:7000"));
        exCred.put("jmsJndiTlsUris", Arrays.asList("smfs://192.168.1.51:7003", "smfs://192.168.1.51:7003"));
        exCred.put("mqttUris", singletonList("tcp://192.168.1.51:7020"));
        exCred.put("mqttTlsUris", Arrays.asList("ssl://192.168.1.51:7021", "ssl://192.168.1.51:7021"));
        exCred.put("mqttWsUris", singletonList("ws://192.168.1.51:7022"));
        exCred.put("mqttWssUris", Arrays.asList("wss://192.168.1.51:7023", "wss://192.168.1.51:7023"));
        exCred.put("restUris", singletonList("http://192.168.1.51:7018"));
        exCred.put("restTlsUris", singletonList("https://192.168.1.51:7019"));
        exCred.put("amqpUris", singletonList("amqp://192.168.1.51:7016"));
        exCred.put("amqpTlsUris", singletonList("amqps://192.168.1.51:7017"));
        exCred.put("managementHostnames", singletonList("vmr-Medium-VMR-02"));
        exCred.put("managementUsername", "sample-mgmt-username2");
        exCred.put("managementPassword", "sample-mgmt-password2");
        exCred.put("activeManagementHostname", "vmr-medium-web2");

        exVcapServices.put("credentials", exCred);
        exVcapServices.put("label", "user-provided");
        exVcapServices.put("name", "internal-solace-pubsub");
        exVcapServices.put("binding_name", null);
        // no need to check for tags in terms of validation. It's more for
        exVcapServices.put("tags", singletonList("solace-pubsub"));

        return exVcapServices;
    }

    EnvConfig getOneUserProvidedSolaceService(String envName) {
        Map<String, Object> services = createOneUserProvidedService();
        JSONObject jsonMapObject = new JSONObject(services);
        String JSONString = jsonMapObject.toString();
        return new EnvConfig(envName, "{ \"user-provided\": [" + JSONString + "] }");
    }

    void load(String... environment) {
        load(EmptyConfiguration.class, environment);
    }

    void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        TestPropertyValues.of(environment).applyTo(applicationContext);
        applicationContext.register(config);
        applicationContext.register(configClass);
        applicationContext.refresh();
        this.context = applicationContext;
    }

    static Set<Object[]> getTestParameters(Set<ResolvableType> testClasses) {
        Set<Object[]> parameters = new HashSet<>();
        for (ResolvableType resolvableRawClass : testClasses) {
            Class<?> rawClass = resolvableRawClass.resolve();
            StringBuilder testName = new StringBuilder(rawClass.getSimpleName());
            for (ResolvableType resolvableGeneric : resolvableRawClass.getGenerics()) {
                Class<?> genericClass = resolvableGeneric.getRawClass();
                if (genericClass != null) testName = testName.append('—').append(genericClass.getSimpleName());
            }

            parameters.add(new Object[]{testName.toString(), rawClass});
        }
        return parameters;
    }

    public record EnvConfig(String envName, String envValue) {
    }
}
