package com.leofee.rocketmq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * Rocket 事务消息
 *
 * @author leofee
 */
public class RocketTransactionMessageTest {


    @Test
    public void producer() throws MQClientException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TransactionMQProducer producer = new TransactionMQProducer("transaction_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {

                System.out.println("executeLocalTransaction -> message:" + msg.getTransactionId());

                return LocalTransactionState.UNKNOW;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {

                System.out.println("checkLocalTransaction -> message:" + msg.getTransactionId());

                return LocalTransactionState.UNKNOW;
            }
        });
        producer.start();
        Message message = new Message("transaction_message_topic", "hello transaction message".getBytes(StandardCharsets.UTF_8));
        producer.sendMessageInTransaction(message, "hello transaction message");
        latch.await();

    }
}
