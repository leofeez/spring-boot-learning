package com.leofee.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author leofee
 */
public class RocketMqClientTest {

    @Test
    public void producer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("hello_world_group");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.start();
        Message message = new Message("hello_world", "hello rocketmq".getBytes());
        // 同步消息发送
        SendResult sendResult = producer.send(message);
        System.out.println(sendResult);

        // 批量发送
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messageList.add(new Message("hello_world", ("hello rocketmq" + i).getBytes()));
        }
        SendResult sendBatch = producer.send(messageList);
        System.out.println(sendBatch);

        // 异步发送
        producer.send(new Message("hello_world", "hello rocketmq async".getBytes(StandardCharsets.UTF_8)), new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("async send result" + sendResult);
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        producer.shutdown();
    }

    @Test
    public void consumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr("192.168.248.131:9876");
        consumer.setConsumerGroup("hello_world_consumer_group");
        consumer.subscribe("hello_world", "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println(new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        while(true) {}
    }
}
