package com.leofeetest;

import com.leofee.activemq.ActiveMqConfig;
import lombok.Data;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ActiveMqConfig.class)
public class ActiveAckTest {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;


    @Test
    public void producer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();

        // 1. 以开启事务方式创建 producer
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue("leofee_ack");
        MessageProducer producer = session.createProducer(queue);

        // 设置消息优先级
//        producer.setPriority(2);

        // 设置消息的存活时间
//        producer.setTimeToLive(1000);

        for (int i = 0; i < 20; i++) {
            // 字符类型消息 TextMessage
            // Message message = session.createTextMessage("hello " + i);

            // 对象类型的消息 ObjectMessage
            Person person = new Person();
            person.setName("leofee " + i + " 号");
            person.setAge(i);
            Message message = session.createObjectMessage(person);

            // 字节类型的消息 ByteMessage

            if (i % 2 == 0) {
                if (i % 6 == 0) {
                    message.setJMSPriority(1);
                } else {
                    message.setJMSPriority(9);
                }
                producer.send(queue, message);
                // 2-1. commit 后消息才会进入消息队列
                session.commit();
            } else {
                producer.send(queue, message);
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

        // 消费者以非事务方式消费消息，指定ACK模式为手动ACK，则需要显示调用message.acknowledge()
        // 消费者以开启事务方式消费消息，手动commit，消息会自动ACK
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue("leofee_ack");
        MessageConsumer consumer = session.createConsumer(queue);

        while (true) {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                TextMessage receive = (TextMessage) message;
                String text = receive.getText();
                System.out.println(text);
            } else if (message instanceof ObjectMessage) {
                ObjectMessage receive = (ObjectMessage) message;
                Person person = (Person) receive.getObject();
                System.out.println(person.toString());
            }

            // 消息应答
            message.acknowledge();
            session.commit();
        }
    }

    @Test
    public void listener() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        // 消费者以非事务方式消费消息，指定ACK模式为手动ACK
        // 消费者以开启事务方式消费消息，则需要手动commit，消息才会ACK
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue("leofee_ack");
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                TextMessage receive = (TextMessage) message;
                String text = null;
                try {
                    text = receive.getText();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                System.out.println(text);
            } else if (message instanceof ObjectMessage) {
                ObjectMessage receive = (ObjectMessage) message;
                Person person = null;
                try {
                    person = (Person) receive.getObject();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                System.out.println(person.toString());
            }

            // 消息应答
            try {
                message.acknowledge();
                session.commit();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        System.out.println("--------------");
        TimeUnit.SECONDS.sleep(10);
    }

    @Data
    static class Person implements Serializable {
        private String name;
        private int age;
    }
}
