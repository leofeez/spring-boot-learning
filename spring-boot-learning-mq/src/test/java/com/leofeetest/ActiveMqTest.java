package com.leofeetest;

import org.junit.Test;

import javax.jms.*;

public class ActiveMqTest extends MqBaseTest {

    @Test
    public void producer() throws Exception {
        // 获取一个连接
        Connection connection = activeMQConnectionFactory.createConnection();

        // 以非事务方式(transacted = false)创建 producer
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // 创建队列
        Queue queue = session.createQueue("leofee_queue");

        // 创建消息的生产者
        MessageProducer producer = session.createProducer(queue);

        // 默认消息的持久化是开启的
        // 可通过设置DeliveryMode.NON_PERSISTENT
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        // 发送消息到队列
        TextMessage textMessage = session.createTextMessage("hello");
        producer.send(queue, textMessage);

        // MapMessage，接口为 k-v 键值对
        MapMessage mapMessage = session.createMapMessage();
        mapMessage.setString("k", "world");
        producer.send(mapMessage);

        ObjectMessage objectMessage = session.createObjectMessage();
        ActiveAckTest.Person person = new ActiveAckTest.Person();
        person.setName("leofee");
        person.setAge(18);
        objectMessage.setObject(person);
        producer.send(objectMessage);
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

            // receive 不指定时间，则该方法会阻塞，直到接收到消息
            Message message = consumer.receive();

            // receive 支持指定超时时间，当超过指定时间后，receive 会返回 null
            // Message messageWithTimeOut = consumer.receive(10000);
            System.out.println("messageId:" + message.getJMSMessageID());
            System.out.println("deliveryMode:" + message.getJMSDeliveryMode());
            System.out.println("timestamp:" + message.getJMSTimestamp());
            System.out.println("priority:" + message.getJMSPriority());

            if (message instanceof TextMessage) {
                String text = ((TextMessage)message).getText();
                System.out.println(text);
            }

            if (message instanceof MapMessage) {
                String text = ((MapMessage)message).getString("k");
                System.out.println(text);
            }

            if (message instanceof ObjectMessage) {
                Object text = ((ObjectMessage)message).getObject();
                System.out.println(text);
            }
        }
    }

    @Test
    public void listener() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("leofee_queue");

        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            try {
                // receive 支持指定超时时间，当超过指定时间后，receive 会返回 null
                // Message messageWithTimeOut = consumer.receive(10000);
                System.out.println("messageId:" + message.getJMSMessageID());
                System.out.println("deliveryMode:" + message.getJMSDeliveryMode());
                System.out.println("timestamp:" + message.getJMSTimestamp());
                System.out.println("priority:" + message.getJMSPriority());

                if (message instanceof TextMessage) {
                    String text = ((TextMessage)message).getText();
                    System.out.println(text);
                }

                if (message instanceof MapMessage) {
                    String text = ((MapMessage)message).getString("k");
                    System.out.println(text);
                }

                if (message instanceof ObjectMessage) {
                    Object text = ((ObjectMessage)message).getObject();
                    System.out.println(text);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
    }
}
