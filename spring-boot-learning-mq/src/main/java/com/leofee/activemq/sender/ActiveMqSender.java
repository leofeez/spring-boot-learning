package com.leofee.activemq.sender;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.TextMessage;

/**
 * @author leofee
 */
@Service
public class ActiveMqSender {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String destination, String message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }

    public void send2(String destination, String message) {
        jmsTemplate.send(new ActiveMQQueue(destination) , session -> {

            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(message);

            return textMessage;
        });
    }

    public void send3(String destination, String message) {
        jmsTemplate.send(new ActiveMQTopic(destination) , session -> {

            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(message);

            return textMessage;
        });
    }
}
