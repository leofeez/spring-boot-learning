package com.leofeetest;

import com.leofee.ActiveMqConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ActiveMqConfig.class)
public class ActiveMqTest {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;


    @Test
    public void producer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();

        // 以非事务方式创建 producer
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("leofee_queue");

        MessageProducer producer = session.createProducer(queue);

        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        for (int i = 0; i < 10; i++) {
            TextMessage textMessage = session.createTextMessage("hello");

            producer.send(queue, textMessage);
        }

        connection.close();
    }

    @Test
    public void consumer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


        Queue queue = session.createQueue("leofee_queue");

        MessageConsumer consumer = session.createConsumer(queue);

        while(true) {
            TextMessage receive = (TextMessage)consumer.receive();
            String text = receive.getText();
            System.out.println(text);
        }




    }
}
