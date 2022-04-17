package com.leofeetest;

import com.leofee.ActiveMqConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.util.concurrent.atomic.LongAdder;

@SpringBootTest(classes = ActiveMqConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ActiveMqTopicTest {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;


    @Test
    public void producer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();

        // 1. 以开启事务方式创建 producer
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Topic topic = session.createTopic("leofee_topic");
        MessageProducer producer = session.createProducer(topic);

        for (int i = 0; i < 20; i++) {
            // 字符类型消息 TextMessage
            Message message = session.createTextMessage("hello " + i);

            producer.send(topic, message);
            if (i % 2 == 0) {
                // 2-1. commit 后消息才会进入消息队列
                session.commit();
            } else {
                // 2-2. rollback 后消息不会进入消息队列
                session.rollback();
            }
        }
        connection.close();
    }

    @Test
    public void consumer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        // 消费者以非事务方式消费消息，指定ACK模式为手动ACK
        // 消费者以开启事务方式消费消息，则需要手动commit，消息才会ACK
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Topic topic = session.createTopic("leofee_topic");
        MessageConsumer consumer = session.createConsumer(topic);

        LongAdder adder = new LongAdder();
        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                TextMessage receive = (TextMessage) message;
                String text = receive.getText();
                System.out.println(text);
            } else if (message instanceof ObjectMessage) {
                ObjectMessage receive = (ObjectMessage) message;
                ActiveAckTest.Person person = (ActiveAckTest.Person) receive.getObject();
                System.out.println(person.toString());
            }

            // 消息应答
            message.acknowledge();
            session.commit();
            adder.increment();
        }
    }
}
