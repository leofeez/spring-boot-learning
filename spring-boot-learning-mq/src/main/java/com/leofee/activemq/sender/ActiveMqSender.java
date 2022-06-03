package com.leofee.activemq.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author leofee
 */
@Service
public class ActiveMqSender {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    public void send(String destination, String message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }
}
