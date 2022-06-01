package com.leofee.activemq;

import org.junit.Test;

import javax.jms.*;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = ActiveMqConfig.class)
public class ActiveTransactionTest extends MqBaseTest {

    @Test
    public void producer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();

        // 以非事务方式创建 producer
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

        Queue queue = session.createQueue("leofee_trx");

        MessageProducer producer = session.createProducer(queue);

        TextMessage textMessage = session.createTextMessage("hello");

        producer.send(queue, textMessage);

        // commit 后消息才会进入消息队列
        session.commit();

        connection.close();
    }

    @Test
    public void consumer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        // 消费者开启事务消费消息，则必须commit，消息才会从队列中移除
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("leofee_trx");

        MessageConsumer consumer = session.createConsumer(queue);

        TextMessage message = (TextMessage) consumer.receive();
        String text = message.getText();
        System.out.println("message not commit:" + text);

        System.out.println("wait 5s");
        Thread.sleep(5000);

        // 消费者提交事务
        System.out.println("message now commit:" + text);
        session.commit();
    }
}
