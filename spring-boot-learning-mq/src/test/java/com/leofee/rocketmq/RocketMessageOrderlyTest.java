package com.leofee.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * RocketMQ中消息的消费顺序
 *
 * @author leofee
 */
public class RocketMessageOrderlyTest {

    @Test
    public void producer() throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer("orderly_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.start();

        for (int i = 0; i < 20; i++) {
            Message message = new Message("orderly_topic", ("order_" + i).getBytes());
            producer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    // 这里可以根据一定的规则将消息发送到指定的Queue中
                    return mqs.get(0);
                }
            }, "");
        }
        producer.shutdown();
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("orderly_consumer_group");
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.subscribe("orderly_topic", "*");
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

                for (MessageExt msg : msgs) {
                    String messageBody = new String(msg.getBody());
                    int queueId = msg.getQueueId();
                    String name = Thread.currentThread().getName();
                    System.out.println("messageBody = " + messageBody + " queueId = " + queueId + " name = " + name);
                }

                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
        new CountDownLatch(1).await();
    }
}
