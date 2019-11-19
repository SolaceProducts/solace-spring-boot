package com.solace.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import com.solacesystems.jms.SolConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class SolaceJmsAutoConfigurationBaseTest extends SolaceJmsAutoConfigurationTestBase {
    private SolaceJmsProperties solaceJmsProperties = getSolaceJmsProperties();
    private SolaceJmsAutoConfigurationBase jmsAutoConfBase;
    private SolaceServiceCredentials solaceServiceCredentials;
    private ObjectMapper objectMapper = new ObjectMapper();

    public SolaceJmsAutoConfigurationBaseTest() {
        super(SolaceJmsAutoConfigurationBase.class);
    }

    @Before
    public void setup() {
        SolaceServiceCredentialsImpl solaceServiceCredentialsImpl = objectMapper
                .convertValue(createOneService().get("credentials"), SolaceServiceCredentialsImpl.class);
        solaceServiceCredentialsImpl.setId("test-id");
        solaceServiceCredentials = solaceServiceCredentialsImpl;
        List<SolaceServiceCredentials> credsList = Collections.singletonList(solaceServiceCredentials);

        jmsAutoConfBase = Mockito.mock(SolaceJmsAutoConfigurationBase.class, Mockito.CALLS_REAL_METHODS);
        jmsAutoConfBase.setProperties(solaceJmsProperties);
        Mockito.doReturn(solaceServiceCredentials).when(jmsAutoConfBase).findFirstSolaceServiceCredentialsImpl();
        Mockito.doReturn(credsList).when(jmsAutoConfBase).getSolaceServiceCredentials();
    }

    @Test
    public void testFindFirstSolaceServiceCredentials() {
        assertEquals(solaceServiceCredentials, jmsAutoConfBase.findFirstSolaceServiceCredentials());
    }

    @Test
    public void testGetSolaceServiceCredentials() {
        assertEquals(Collections.singletonList(solaceServiceCredentials), jmsAutoConfBase.getSolaceServiceCredentials());
    }

    @Test
    public void testGetSolConnectionFactory() {
        validateSolConnectionFactory(jmsAutoConfBase.getSolConnectionFactory(), false);
        disableSolaceServiceCredentials();
        validateSolConnectionFactory(jmsAutoConfBase.getSolConnectionFactory(), true);
    }

    @Test
    public void testGetSolConnectionFactoryByCreds() {
        validateSolConnectionFactory(jmsAutoConfBase.getSolConnectionFactory(solaceServiceCredentials), false);
        disableSolaceServiceCredentials();
        validateSolConnectionFactory(jmsAutoConfBase.getSolConnectionFactory(solaceServiceCredentials), false);
    }

    @Test
    public void testGetSolConnectionFactoryById() {
        validateSolConnectionFactory(jmsAutoConfBase.getSolConnectionFactory(solaceServiceCredentials.getId()), false);
        disableSolaceServiceCredentials();
        assertNull((jmsAutoConfBase.getSolConnectionFactory(solaceServiceCredentials.getId())));
    }

    private void validateSolConnectionFactory(SolConnectionFactory solConnectionFactory, boolean isProperties) {
        assertFalse(solConnectionFactory.getDirectTransport());
        if (isProperties) {
            assertEquals(solaceJmsProperties.getHost(), solConnectionFactory.getHost());
            assertEquals(solaceJmsProperties.getMsgVpn(), solConnectionFactory.getVPN());
            assertEquals(solaceJmsProperties.getClientUsername(), solConnectionFactory.getUsername());
            assertEquals(solaceJmsProperties.getClientPassword(), solConnectionFactory.getPassword());
        } else {
            assertEquals(solaceServiceCredentials.getSmfHost(), solConnectionFactory.getHost());
            assertEquals(solaceServiceCredentials.getMsgVpnName(), solConnectionFactory.getVPN());
            assertEquals(solaceServiceCredentials.getClientUsername(), solConnectionFactory.getUsername());
            assertEquals(solaceServiceCredentials.getClientPassword(), solConnectionFactory.getPassword());
        }

    }

    private SolaceJmsProperties getSolaceJmsProperties() {
        SolaceJmsProperties solaceJmsProperties = new SolaceJmsProperties();
        solaceJmsProperties.setClientName("client-password");
        solaceJmsProperties.setClientUsername("client-username");
        solaceJmsProperties.setHost("tcp://localhost");
        solaceJmsProperties.setMsgVpn("msg-vpn-name");
        solaceJmsProperties.setDirectTransport(false);
        return solaceJmsProperties;
    }

    private void disableSolaceServiceCredentials() {
        Mockito.doReturn(null).when(jmsAutoConfBase).findFirstSolaceServiceCredentialsImpl();
        Mockito.doReturn(new ArrayList<>()).when(jmsAutoConfBase).getSolaceServiceCredentials();
    }
}
