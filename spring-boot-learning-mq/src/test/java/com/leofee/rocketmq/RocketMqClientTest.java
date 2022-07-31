package com.leofee.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author leofee
 */
public class RocketMqClientTest {

    @Test
    public void producer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("hello_world_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.setRetryTimesWhenSendFailed(3);

        producer.setRetryAnotherBrokerWhenNotStoreOK(true);
        producer.start();
        Message message = new Message("hello_world", "hello rocketmq".getBytes());
        // 同步消息发送
        SendResult sendResult = producer.send(message);
        System.out.println("send result: " + sendResult);

        // 批量发送
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messageList.add(new Message("hello_world", ("hello rocketmq" + i).getBytes()));
        }
        SendResult sendBatch = producer.send(messageList);
        System.out.println("batch send result :" + sendBatch);

        // 异步发送
        producer.send(new Message("hello_world", "hello rocketmq async".getBytes(StandardCharsets.UTF_8)), new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("async send result: " + sendResult);
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        // 阻塞
        new CountDownLatch(1).await();
        producer.shutdown();
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.setConsumerGroup("hello_world_consumer_group");
        consumer.subscribe("hello_world", "*");
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println(new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        new CountDownLatch(1).await();
    }

    @Test
    public void producer02() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer("hello_world_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.start();

        Message message = new Message("test_tag", "TAG-A", "hello world rocket tag filter".getBytes());
        SendResult sendResult = producer.send(message);
        System.out.println("producer02 send result: " + sendResult);

        // 批量发送
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messageList.add(new Message("test_tag", "TAG-B", ("hello rocketmq" + i).getBytes()));
        }
        SendResult sendBatch = producer.send(messageList);
        System.out.println("producer02 send result: " + sendBatch);

        new CountDownLatch(1).await();
    }

    @Test
    public void consumer02() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

            for (MessageExt msg : msgs) {
                System.out.println(new String(msg.getBody()));
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        consumer.subscribe("test_tag", "TAG-A");

        new CountDownLatch(1).await();
    }

    @Test
    public void producer03() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer("hello_world_producer_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.start();

        // 批量发送
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Message message = new Message("test_selector", "TAG-B", ("hello rocketmq" + i).getBytes());
            // 指定对应的属性
            message.putUserProperty("order", i + "");
            messageList.add(message);
        }
        SendResult sendBatch = producer.send(messageList);
        System.out.println("producer02 send result: " + sendBatch);

        new CountDownLatch(1).await();
    }

    @Test
    public void consumer03() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hello_world_consumer_group");
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

            for (MessageExt msg : msgs) {
                System.out.println(new String(msg.getBody()));
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        // 创建对应的过滤器
        MessageSelector selector = MessageSelector.bySql("order > 5 and order <= 10 ");
        consumer.subscribe("test_selector", selector);
        consumer.start();

        new CountDownLatch(1).await();
    }
}
