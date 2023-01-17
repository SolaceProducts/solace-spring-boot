package jndidemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@SpringBootApplication
public class JndiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JndiDemoApplication.class, args);
    }

    @Service
    static class MessageProducer implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

        @Autowired
        private JmsTemplate producerJmsTemplate;

        // Examples of other options to get JmsTemplate in a cloud environment with possibly multiple providers available:
        // Use this to access JmsTemplate of the first service found or look up a specific one by SolaceServiceCredentials
        // @Autowired private SpringSolJmsConnectionFactoryCloudFactory springSolJmsConnectionFactoryCloudFactory;
        // @Autowired private SolaceServiceCredentials solaceServiceCredentials;
        // For backwards compatibility:
        // @Autowired(required=false) private SolaceMessagingInfo solaceMessagingInfo;

        @Value("${solace.jms.demoProducerQueueJndiName}")
        private String queueJndiName;

        public void run(String... strings) {
            String msg = "Hello World";
            logger.info("============= Sending " + msg);
            this.producerJmsTemplate.convertAndSend(queueJndiName, msg);
        }
    }

    @Component
    static class MessageHandler {

        private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

        // Retrieve the name of the queue from the application.properties file
        @JmsListener(destination = "${solace.jms.demoConsumerQueueJndiName}", containerFactory = "cFactory")
        public void processMsg(Message<?> msg) {
            StringBuilder msgAsStr = new StringBuilder("============= Received \nHeaders:");
            MessageHeaders hdrs = msg.getHeaders();
            msgAsStr.append("\nUUID: ").append(hdrs.getId());
            msgAsStr.append("\nTimestamp: ").append(hdrs.getTimestamp());
            for (Map.Entry<String, Object> entry : hdrs.entrySet()) {
                msgAsStr.append("\n").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            msgAsStr.append("\nPayload: ").append(msg.getPayload());
            if (!msgAsStr.isEmpty()) {
                logger.info(msgAsStr.toString());
            }
        }
    }

}
