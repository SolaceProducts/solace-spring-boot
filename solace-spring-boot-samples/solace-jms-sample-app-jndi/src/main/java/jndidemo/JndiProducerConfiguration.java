package jndidemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;

@Configuration
public class JndiProducerConfiguration {

    @Value("${solace.jms.demoConnectionFactoryJndiName}")
    private String connectionFactoryJndiName;

    @Autowired
    private JndiTemplate jndiTemplate;

    private JndiObjectFactoryBean producerConnectionFactory() {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiTemplate(jndiTemplate);
        factoryBean.setJndiName(connectionFactoryJndiName);
        // following ensures all the properties are injected before returning
        try {
            factoryBean.afterPropertiesSet();
        } catch (IllegalArgumentException | NamingException e) {
            e.printStackTrace();
        }
        return factoryBean;
    }

    private CachingConnectionFactory producerCachingConnectionFactory() {
        CachingConnectionFactory ccf = new CachingConnectionFactory((ConnectionFactory) producerConnectionFactory().getObject());
        ccf.setSessionCacheSize(10);
        return ccf;
    }

    // Configure the destination resolver for the producer:
    // Here we are using JndiDestinationResolver for JNDI destinations
    // Other options include using DynamicDestinationResolver for non-JNDI destinations
    private JndiDestinationResolver producerJndiDestinationResolver() {
        JndiDestinationResolver jdr = new JndiDestinationResolver();
        jdr.setCache(true);
        jdr.setJndiTemplate(jndiTemplate);
        return jdr;
    }

    @Bean
    public JmsTemplate producerJmsTemplate() {
        JmsTemplate jt = new JmsTemplate(producerCachingConnectionFactory());
        jt.setDeliveryPersistent(true);
        jt.setDestinationResolver(producerJndiDestinationResolver());
        return jt;
    }

}
