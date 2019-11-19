package com.solace.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SolaceJavaAutoConfigurationBaseTest extends SolaceJavaAutoConfigurationTestBase {
    private SolaceJavaProperties solaceJavaProperties = getSolaceJavaProperties();
    private SolaceJavaAutoConfigurationBase jcsmpAutoConfBase;
    private SolaceServiceCredentials solaceServiceCredentials;
    private ObjectMapper objectMapper = new ObjectMapper();

    public SolaceJavaAutoConfigurationBaseTest() {
        super(SolaceJavaAutoConfigurationBase.class);
    }

    @Before
    public void setup() {
        SolaceServiceCredentialsImpl solaceServiceCredentialsImpl = objectMapper
                .convertValue(createOneService().get("credentials"), SolaceServiceCredentialsImpl.class);
        solaceServiceCredentialsImpl.setId("test-id");
        solaceServiceCredentials = solaceServiceCredentialsImpl;
        List<SolaceServiceCredentials> credsList = Collections.singletonList(solaceServiceCredentials);

        jcsmpAutoConfBase = Mockito.mock(SolaceJavaAutoConfigurationBase.class, Mockito.CALLS_REAL_METHODS);
        jcsmpAutoConfBase.setProperties(solaceJavaProperties);
        Mockito.doReturn(solaceServiceCredentials).when(jcsmpAutoConfBase).findFirstSolaceServiceCredentialsImpl();
        Mockito.doReturn(credsList).when(jcsmpAutoConfBase).getSolaceServiceCredentials();
    }

    @Test
    public void testFindFirstSolaceServiceCredentials() {
        assertEquals(solaceServiceCredentials, jcsmpAutoConfBase.findFirstSolaceServiceCredentials());
    }

    @Test
    public void testGetSolaceServiceCredentials() {
        assertEquals(Collections.singletonList(solaceServiceCredentials), jcsmpAutoConfBase.getSolaceServiceCredentials());
    }

    @Test
    public void testGetSpringJCSMPFactory() throws InvalidPropertiesException {
        validateJCSMPFactory(jcsmpAutoConfBase.getSpringJCSMPFactory().createSession(), false);
        disableSolaceServiceCredentials();
        validateJCSMPFactory(jcsmpAutoConfBase.getSpringJCSMPFactory().createSession(), true);
    }

    @Test
    public void testGetSpringJCSMPFactoryByCreds() throws InvalidPropertiesException {
        validateJCSMPFactory(jcsmpAutoConfBase.getSpringJCSMPFactory(solaceServiceCredentials).createSession(), false);
        disableSolaceServiceCredentials();
        validateJCSMPFactory(jcsmpAutoConfBase.getSpringJCSMPFactory(solaceServiceCredentials).createSession(), false);
    }

    @Test
    public void testGetSpringJCSMPFactoryById() throws InvalidPropertiesException {
        validateJCSMPFactory(jcsmpAutoConfBase.getSpringJCSMPFactory(solaceServiceCredentials.getId()).createSession(), false);
        disableSolaceServiceCredentials();
        assertNull(jcsmpAutoConfBase.getSpringJCSMPFactory(solaceServiceCredentials.getId()));
    }

    @Test
    public void testGetJCSMPProperties() throws InvalidPropertiesException {
        validateJCSMPProperties(jcsmpAutoConfBase.getJCSMPProperties(), false);
        disableSolaceServiceCredentials();
        validateJCSMPProperties(jcsmpAutoConfBase.getJCSMPProperties(), true);
    }

    @Test
    public void testGetJCSMPPropertiesByCreds() throws InvalidPropertiesException {
        validateJCSMPProperties(jcsmpAutoConfBase.getJCSMPProperties(solaceServiceCredentials), false);
        disableSolaceServiceCredentials();
        validateJCSMPProperties(jcsmpAutoConfBase.getJCSMPProperties(solaceServiceCredentials), false);
    }

    @Test
    public void testGetJCSMPPropertiesById() throws InvalidPropertiesException {
        validateJCSMPProperties(jcsmpAutoConfBase.getJCSMPProperties(solaceServiceCredentials.getId()), false);
        disableSolaceServiceCredentials();
        assertNull(jcsmpAutoConfBase.getJCSMPProperties(solaceServiceCredentials.getId()));
    }

    private void validateJCSMPFactory(JCSMPSession jcsmpSession, boolean isProperties) {
        validateApiProperties(jcsmpSession);
        validateJCSMPConnectionProperties((JCSMPChannelProperties) jcsmpSession.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES));
        assertEquals(solaceJavaProperties.getClientName(), jcsmpSession.getProperty(JCSMPProperties.CLIENT_NAME));

        if (isProperties) {
            assertEquals(solaceJavaProperties.getHost(), jcsmpSession.getProperty(JCSMPProperties.HOST));
            assertEquals(solaceJavaProperties.getMsgVpn(), jcsmpSession.getProperty(JCSMPProperties.VPN_NAME));
            assertEquals(solaceJavaProperties.getClientUsername(), jcsmpSession.getProperty(JCSMPProperties.USERNAME));
            assertEquals(solaceJavaProperties.getClientPassword(), jcsmpSession.getProperty(JCSMPProperties.PASSWORD));
        } else {
            assertEquals(solaceServiceCredentials.getSmfHost(), jcsmpSession.getProperty(JCSMPProperties.HOST));
            assertEquals(solaceServiceCredentials.getMsgVpnName(), jcsmpSession.getProperty(JCSMPProperties.VPN_NAME));
            assertEquals(solaceServiceCredentials.getClientUsername(), jcsmpSession.getProperty(JCSMPProperties.USERNAME));
            assertEquals(solaceServiceCredentials.getClientPassword(), jcsmpSession.getProperty(JCSMPProperties.PASSWORD));
        }

    }

    private void validateJCSMPProperties(JCSMPProperties jcsmpProperties, boolean isProperties)
            throws InvalidPropertiesException {
        validateApiProperties(jcsmpProperties);
        validateJCSMPConnectionProperties((JCSMPChannelProperties) jcsmpProperties.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES));
        assertEquals(solaceJavaProperties.getClientName(), jcsmpProperties.getProperty(JCSMPProperties.CLIENT_NAME));

        if (isProperties) {
            assertEquals(solaceJavaProperties.getHost(), jcsmpProperties.getProperty(JCSMPProperties.HOST));
            assertEquals(solaceJavaProperties.getMsgVpn(), jcsmpProperties.getProperty(JCSMPProperties.VPN_NAME));
            assertEquals(solaceJavaProperties.getClientUsername(), jcsmpProperties.getProperty(JCSMPProperties.USERNAME));
            assertEquals(solaceJavaProperties.getClientPassword(), jcsmpProperties.getProperty(JCSMPProperties.PASSWORD));
        } else {
            assertEquals(solaceServiceCredentials.getSmfHost(), jcsmpProperties.getProperty(JCSMPProperties.HOST));
            assertEquals(solaceServiceCredentials.getMsgVpnName(), jcsmpProperties.getProperty(JCSMPProperties.VPN_NAME));
            assertEquals(solaceServiceCredentials.getClientUsername(), jcsmpProperties.getProperty(JCSMPProperties.USERNAME));
            assertEquals(solaceServiceCredentials.getClientPassword(), jcsmpProperties.getProperty(JCSMPProperties.PASSWORD));
        }
    }

    private void validateApiProperties(JCSMPProperties properties) throws InvalidPropertiesException {
        validateApiProperties(new SpringJCSMPFactory(properties).createSession());
    }

    private void validateApiProperties(JCSMPSession session) {
        for (Map.Entry<String,String> entry : solaceJavaProperties.getApiProperties().entrySet()) {
            assertNotNull(session.getProperty("jcsmp." + entry.getKey()));
            assertEquals(session.getProperty("jcsmp." + entry.getKey()), entry.getValue());
        }
    }

    private void validateJCSMPConnectionProperties(JCSMPChannelProperties channelProperties) {
        assertEquals(solaceJavaProperties.getConnectRetries(), channelProperties.getConnectRetries());
        assertEquals(solaceJavaProperties.getReconnectRetries(), channelProperties.getReconnectRetries());
        assertEquals(solaceJavaProperties.getConnectRetriesPerHost(), channelProperties.getConnectRetriesPerHost());
        assertEquals(solaceJavaProperties.getReconnectRetryWaitInMillis(), channelProperties.getReconnectRetryWaitInMillis());
    }

    private SolaceJavaProperties getSolaceJavaProperties() {
        SolaceJavaProperties solaceJavaProperties = new SolaceJavaProperties();
        solaceJavaProperties.setClientName("client-name");
        solaceJavaProperties.setClientPassword("client-password");
        solaceJavaProperties.setClientUsername("client-username");
        solaceJavaProperties.setHost("localhost");
        solaceJavaProperties.setMsgVpn("msg-vpn-name");
        solaceJavaProperties.setConnectRetries(10);
        solaceJavaProperties.setConnectRetriesPerHost(10);
        solaceJavaProperties.setReconnectRetries(10);
        solaceJavaProperties.setReconnectRetryWaitInMillis(100);
        return solaceJavaProperties;
    }

    private void disableSolaceServiceCredentials() {
        Mockito.doReturn(null).when(jcsmpAutoConfBase).findFirstSolaceServiceCredentialsImpl();
        Mockito.doReturn(new ArrayList<>()).when(jcsmpAutoConfBase).getSolaceServiceCredentials();
    }
}
