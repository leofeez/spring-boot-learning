package com.leofee.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Rocket 事务消息
 *
 * @author leofee
 */
public class RocketTransactionMessageTest {

    int seconds = 20;

    @Test
    public void producer() throws MQClientException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TransactionMQProducer producer = new TransactionMQProducer("transaction_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");

        // Transaction Listener
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                System.out.println("executeLocalTransaction -> args:" + arg);
                System.out.println("executeLocalTransaction -> message:" + msg);
                System.out.println("executeLocalTransaction -> message transaction id :" + msg.getTransactionId());

                // 执行本地事务操作 10s
                for (int i = seconds; i > 0; i--) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("事务操作剩余 " + i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                }

                /*
                 * COMMIT_MESSAGE: 表示事务可以提交
                 * ROLLBACK_MESSAGE: 表示事务需要回滚
                 * UNKNOW: 表示事务需要等待
                 */
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {

                System.out.println("checkLocalTransaction -> message transaction id:" + msg.getTransactionId());
                if (seconds > 0) {
                    return LocalTransactionState.UNKNOW;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        producer.start();
        Message message = new Message("transaction_message_topic", "hello transaction message".getBytes(StandardCharsets.UTF_8));

        // Transaction message
        producer.sendMessageInTransaction(message, "hello transaction message");
        latch.await();

    }

    @Test
    public void consumer() throws MQClientException, InterruptedException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("transaction_consumer_group");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println("消费者收到消息：" +  msg);
                System.out.println("消费者收到消息体为：" +  new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.subscribe("transaction_message_topic", "*");
        consumer.start();

        new CountDownLatch(1).await();
    }
}
