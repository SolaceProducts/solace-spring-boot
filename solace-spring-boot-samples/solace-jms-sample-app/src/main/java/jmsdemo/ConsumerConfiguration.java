package jmsdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@EnableJms
@Configuration
public class ConsumerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerConfiguration.class);

    // Example configuration of the ConnectionFactory: we instantiate it here ourselves and set an error handler
    @Bean
    public DefaultJmsListenerContainerFactory cFactory(ConnectionFactory connectionFactory, DemoErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setErrorHandler(errorHandler);
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
