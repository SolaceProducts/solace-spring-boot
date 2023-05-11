package com.solace.spring.boot.autoconfigure;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solace.services.core.model.SolaceServiceCredentialsImpl;
import com.solace.spring.cloud.core.SolaceServiceCredentialsFactory;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolConnectionFactoryImpl;
import com.solacesystems.jms.SpringSolJmsConnectionFactoryCloudFactory;
import com.solacesystems.jms.SpringSolJmsJndiTemplateCloudFactory;
import com.solacesystems.jms.property.JMSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jndi.JndiTemplate;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

abstract class SolaceJmsAutoConfigurationBase implements SpringSolJmsConnectionFactoryCloudFactory, SpringSolJmsJndiTemplateCloudFactory {
    private static final Logger logger = LoggerFactory.getLogger(SolaceJmsAutoConfigurationBase.class);

    private SolaceJmsProperties properties;

    SolaceJmsAutoConfigurationBase(SolaceJmsProperties properties) {
        this.properties = properties;
    }

    abstract SolaceServiceCredentials findFirstSolaceServiceCredentialsImpl();

    @Override
    public abstract List<SolaceServiceCredentials> getSolaceServiceCredentials();

    @Bean
    @ConditionalOnMissingBean
    @Override
    public SolaceServiceCredentials findFirstSolaceServiceCredentials() {
        return findFirstSolaceServiceCredentialsImpl();
    }

    @Bean
    @Override
    public SolConnectionFactory getSolConnectionFactory() {
        return getSolConnectionFactory(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public SolConnectionFactory getSolConnectionFactory(String id) {
        SolaceServiceCredentials solaceServiceCredentials = findSolaceServiceCredentialsById(id);
        return solaceServiceCredentials == null ? null : getSolConnectionFactory(solaceServiceCredentials);
    }

    @Override
    public SolConnectionFactory getSolConnectionFactory(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            Hashtable<String, String> ht = new Hashtable<>(properties.getApiProperties());
            JMSProperties props = new JMSProperties(ht);
            props.initialize();
            SolConnectionFactoryImpl cf = new SolConnectionFactoryImpl(props);
            SolaceServiceCredentials credentials = solaceServiceCredentials != null ?
                    solaceServiceCredentials : new SolaceServiceCredentialsImpl();

            cf.setHost(credentials.getSmfHost() != null ?
                    credentials.getSmfHost() : properties.getHost());

            cf.setVPN(credentials.getMsgVpnName() != null ?
                    credentials.getMsgVpnName() : properties.getMsgVpn());

            cf.setUsername(credentials.getClientUsername() != null ?
                    credentials.getClientUsername() : properties.getClientUsername());

            cf.setPassword(credentials.getClientPassword() != null ?
                    credentials.getClientPassword() : properties.getClientPassword());

            cf.setDirectTransport(properties.isDirectTransport());
            return cf;
        } catch (Exception ex) {
            logger.error("Exception found during Solace Connection Factory creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "connection factory, ensure that the sol-jms-jakarta-<version>.jar " + "is the classpath", ex);
        }
    }

    private SolaceServiceCredentials findSolaceServiceCredentialsById(String id) {
        for (SolaceServiceCredentials credentials : getSolaceServiceCredentials()) {
            if (credentials.getId().equals(id)) return credentials;
        }
        return null;
    }


    void setProperties(SolaceJmsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Override
    public JndiTemplate getJndiTemplate() {
        return getJndiTemplate(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public JndiTemplate getJndiTemplate(SolaceServiceCredentials solaceServiceCredentials) {
        try {
            SolaceServiceCredentials credentials = solaceServiceCredentials != null ?
                    solaceServiceCredentials : new SolaceServiceCredentialsImpl();

            Properties env = new Properties();
            env.putAll(properties.getApiProperties());
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
            env.put(InitialContext.PROVIDER_URL, credentials.getJmsJndiUri() != null ?
                    credentials.getJmsJndiUri() : properties.getHost());
            env.put(Context.SECURITY_PRINCIPAL,
                    credentials.getClientUsername() != null && credentials.getMsgVpnName() != null ?
                            credentials.getClientUsername() + '@' + credentials.getMsgVpnName() :
                            properties.getClientUsername() + '@' + properties.getMsgVpn());

            env.put(Context.SECURITY_CREDENTIALS, credentials.getClientPassword() != null ?
                    credentials.getClientPassword() : properties.getClientPassword());

            JndiTemplate jndiTemplate = new JndiTemplate();
            jndiTemplate.setEnvironment(env);
            return jndiTemplate;
        } catch (Exception ex) {
            logger.error("Exception found during Solace JNDI Initial Context creation.", ex);
            throw new IllegalStateException("Unable to create Solace "
                    + "JNDI Initial Context, ensure that the sol-jms-jakarta-<version>.jar " + "is the classpath", ex);
        }
    }

    @Override
    public JndiTemplate getJndiTemplate(String id) {
        List<SolaceServiceCredentials> credentials = SolaceServiceCredentialsFactory.getAllFromCloudFoundry();
        return credentials.size() == 0 ? null : getJndiTemplate(credentials.get(0));
    }
}
