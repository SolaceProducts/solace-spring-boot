package jmsdemo;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;


@Configuration
public class ProducerConfiguration {

    // Example use of CachingConnectionFactory for the producer
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        CachingConnectionFactory ccf = new CachingConnectionFactory(connectionFactory);
        return new JmsTemplate(ccf);
    }
}
