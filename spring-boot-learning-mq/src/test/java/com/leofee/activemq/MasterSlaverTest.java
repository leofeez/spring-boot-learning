package com.leofee.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author leofee
 */
public class MasterSlaverTest {


    @Test
    public void producer() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin123", "failover:(tcp://localhost:61616,tcp://localhost:61617)");

        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("master_slaver_queue");

        TextMessage textMessage = session.createTextMessage();
        textMessage.setText("hello master slaver");

        MessageProducer producer = session.createProducer(queue);
        producer.send(textMessage);

        new CountDownLatch(1).await();
    }
}
