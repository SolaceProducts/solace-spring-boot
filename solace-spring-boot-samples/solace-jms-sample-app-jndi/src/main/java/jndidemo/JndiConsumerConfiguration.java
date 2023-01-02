package jndidemo;

import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@EnableJms
@Configuration
public class JndiConsumerConfiguration {

    @Value("${solace.jms.demoConnectionFactoryJndiName}")
    private String connectionFactoryJndiName;

    @Autowired
    private JndiTemplate jndiTemplate;

    private static final Logger logger = LoggerFactory.getLogger(JndiConsumerConfiguration.class);

    private JndiObjectFactoryBean consumerConnectionFactory() {
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

    // Configure the destination resolver for the consumer:
    // Here we are using JndiDestinationResolver for JNDI destinations
    // Other options include using DynamicDestinationResolver for non-JNDI destinations
    private JndiDestinationResolver consumerJndiDestinationResolver() {
        JndiDestinationResolver jdr = new JndiDestinationResolver();
        jdr.setCache(true);
        jdr.setJndiTemplate(jndiTemplate);
        return jdr;
    }

    // Example configuration of the JmsListenerContainerFactory
    @Bean
    public DefaultJmsListenerContainerFactory cFactory(DemoErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory((ConnectionFactory) consumerConnectionFactory().getObject());
        factory.setDestinationResolver(consumerJndiDestinationResolver());
        factory.setErrorHandler(errorHandler);
        factory.setConcurrency("3-10");
        return factory;
    }

    @Service
    public static class DemoErrorHandler implements ErrorHandler {

        public void handleError(Throwable t) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            t.printStackTrace(ps);
            String output = os.toString(StandardCharsets.UTF_8);
            logger.error("============= Error processing message: " + t.getMessage() + "\n" + output);

        }
    }

}
