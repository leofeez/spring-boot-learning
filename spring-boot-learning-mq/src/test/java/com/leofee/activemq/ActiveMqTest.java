package com.leofee.activemq;

import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Test;

import javax.jms.*;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

public class ActiveMqTest extends ActiveMqBaseTest {

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
        producer.setPriority(9);

        // 发送消息到队列
        TextMessage textMessage = session.createTextMessage("hello");
        producer.send(queue, textMessage);

        // MapMessage，接口为 k-v 键值对
        MapMessage mapMessage = session.createMapMessage();
        mapMessage.setString("hello", "world");
        mapMessage.setString("world", "hello");
        mapMessage.setBoolean("happy", true);
        producer.send(mapMessage);

        ObjectMessage objectMessage = session.createObjectMessage();
        ActiveAckTest.Person person = new ActiveAckTest.Person();
        person.setName("leofee");
        person.setAge(18);
        objectMessage.setObject(person);
        producer.send(objectMessage, DeliveryMode.NON_PERSISTENT, 9, 1000);
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
            System.out.println("接收到消息：" + message);

            // receive 支持指定超时时间，当超过指定时间后，receive 会返回 null
            // Message messageWithTimeOut = consumer.receive(10000);
//            System.out.println("time:" + message.getJMSTimestamp());
//            System.out.println("brokerInTime:" + ((ActiveMQMessage)message).getBrokerInTime());
//            System.out.println("brokerOutTime:" + ((ActiveMQMessage)message).getBrokerOutTime());
//            System.out.println("messageId:" + message.getJMSMessageID());
//            System.out.println("deliveryMode:" + message.getJMSDeliveryMode());
//            System.out.println("timestamp:" + message.getJMSTimestamp());
            System.out.println("priority:" + message.getJMSPriority());

            if (message instanceof TextMessage) {
                String text = ((TextMessage)message).getText();
                System.out.println(text);
            }

            // MapMessage
            if (message instanceof MapMessage) {
                // 遍历出MapMessage中所有的消息内容
                MapMessage mapMessage = (MapMessage) message;
                for (Enumeration<?> enumeration = mapMessage.getMapNames(); enumeration.hasMoreElements();) {
                    String key = (String) enumeration.nextElement();
                    System.out.print("key:" + key);
                    System.out.println(", value:" + mapMessage.getString(key));
                }
            }

            if (message instanceof ObjectMessage) {
                Object text = ((ObjectMessage)message).getObject();
                System.out.println(text);
            }
        }
    }

    @Test
    public void listener() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("leofee_queue");

        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            try {
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
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    public void producer2() throws Exception {
        Connection connection = activeMQConnectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("leofee_exclusive_queue");
        MessageProducer producer = session.createProducer(queue);

        for (int i = 0; i < 10; i++) {
            TextMessage message = session.createTextMessage();
            message.setText("leofee" + i);
            // 注意是Property，而不是value
            message.setIntProperty("age", i);
            producer.send(message);
        }

        connection.close();
    }

    @Test
    public void delay() throws Exception {
        Connection connection = activeMQConnectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("delay_queue");
        MessageProducer producer = session.createProducer(queue);

        for (int i = 0; i < 100; i++) {
            TextMessage message = session.createTextMessage();
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 10000 * i);
            message.setText("leofee" + i);
            // 注意是Property，而不是value
            message.setIntProperty("age", i);
            producer.send(message);
        }
        connection.close();
    }

    @Test
    public void delayConsumer() throws Exception {
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("delay_queue");
        MessageConsumer consumer = session.createConsumer(queue);

        while (true) {
            TextMessage message = (TextMessage)consumer.receive();
            System.out.println(message.getText());
        }
    }

    @Test
    public void consumer2() throws Exception {
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 设置queue对应的消费者是独占消费者 consumer.exclusive=true
        Queue queue = session.createQueue("leofee_exclusive_queue");
        String selector = "age > 18";
        MessageConsumer consumer = session.createConsumer(queue, selector);

        while (true) {
            MapMessage message = (MapMessage)consumer.receive();
            System.out.println(message.getString("name"));
        }
    }

    @Test
    public void consumer3() throws Exception {
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("leofee_exclusive_queue");
        MessageConsumer consumer = session.createConsumer(queue);

        while (true) {
            MapMessage message = (MapMessage)consumer.receive();
            System.out.println(message.getString("name"));
        }
    }

    @Test
    public void reply() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("leofee_queue");
        MessageProducer producer = session.createProducer(queue);
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText("hello");
        textMessage.setJMSReplyTo(new ActiveMQQueue("leofee_reply"));
        producer.send(textMessage);

        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(message -> {
            TextMessage receivedMessage = (TextMessage)message;
            try {
                System.out.println("接收到消息：" + receivedMessage.getText());

                // 获取接收到消息的 Reply To
                Destination replyTo = receivedMessage.getJMSReplyTo();
                System.out.println("reply to：" + ((ActiveMQQueue)replyTo).getQueueName());

                // 创建 Reply To 的 Producer
                MessageProducer replyProducer = session.createProducer(replyTo);
                TextMessage replyToMessage = session.createTextMessage();
                replyToMessage.setText("world");
                replyProducer.send(replyToMessage);

                // 接受 Reply To 的 Consumer
                MessageConsumer replyConsumer = session.createConsumer(replyTo);
                replyConsumer.setMessageListener(replyMessage -> {
                    try {
                        System.out.println("reply message:" + ((TextMessage)replyMessage).getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });

            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        latch.await();
        connection.close();
    }

    @Test
    public void browser() throws Exception {
        Connection connection = this.activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("browser_queue");
        MessageProducer producer = session.createProducer(queue);

        for (int i = 0; i < 10; i++) {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText("hello " + i);
            producer.send(textMessage);
        }

        QueueBrowser browser = session.createBrowser(queue);

        for (Enumeration enumeration = browser.getEnumeration(); enumeration.hasMoreElements();) {
            System.out.println(enumeration.nextElement());
        }
    }

    @Test
    public void requestor() throws Exception {
        QueueConnection connection = this.activeMQConnectionFactory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("requestor_queue");

        QueueRequestor requestor = new QueueRequestor(session, queue);

        TextMessage message = session.createTextMessage();
        message.setText("hello requestor");

        System.out.println("requestor 开始发送消息");
        Message res = requestor.request(message);
        System.out.println("requestor 结束发送消息，reply = " + res);

        connection.close();
    }

    @Test
    public void requestorConsumer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        QueueConnection connection = this.activeMQConnectionFactory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("requestor_queue");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(message -> {
            try {
                System.out.println("requestor consumer 接收到消息" + ((TextMessage)message).getText());
                Destination replyTo = message.getJMSReplyTo();
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText("你好 requestor，我接收到你发送过来的消息了：" + ((TextMessage)message).getText());
                MessageProducer producer = session.createProducer(replyTo);
                producer.send(textMessage);
                connection.close();
                latch.countDown();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        latch.await();
    }
}
