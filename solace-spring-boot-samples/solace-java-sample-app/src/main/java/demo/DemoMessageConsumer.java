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
package demo;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;

public class DemoMessageConsumer implements XMLMessageListener {

    private CountDownLatch latch = new CountDownLatch(1);
    private static final Logger logger = LoggerFactory.getLogger(DemoMessageConsumer.class);

    public void onReceive(BytesXMLMessage msg) {
        if (msg instanceof TextMessage) {
            logger.info("============= TextMessage received: " + ((TextMessage) msg).getText());
        } else {
            logger.info("============= Message received.");
        }
        latch.countDown(); // unblock main thread
    }

    public void onException(JCSMPException e) {
        logger.info("Consumer received exception:", e);
        latch.countDown(); // unblock main thread
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
