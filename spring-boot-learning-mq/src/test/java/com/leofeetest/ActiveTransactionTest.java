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
public class ActiveTransactionTest {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;


    @Test
    public void producer() throws Exception {

        Connection connection = activeMQConnectionFactory.createConnection();

        // 以非事务方式创建 producer
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

        Queue queue = session.createQueue("leofee_trx");

        MessageProducer producer = session.createProducer(queue);

        for (int i = 0; i < 10; i++) {
            TextMessage textMessage = session.createTextMessage("hello");

            producer.send(queue, textMessage);
        }

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

        while(true) {
            TextMessage receive = (TextMessage)consumer.receive();
            String text = receive.getText();
            System.out.println(text);

            // 消费者提交事务
            session.commit();
        }
    }
}
