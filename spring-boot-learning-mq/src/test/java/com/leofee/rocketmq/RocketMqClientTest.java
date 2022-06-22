package com.leofee.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.Test;

/**
 * @author leofee
 */
public class RocketMqClientTest {

    @Test
    public void producer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("test");
        producer.setNamesrvAddr("192.168.248.131:9876");
        producer.start();
        Message message = new Message("test", "hello rocketmq".getBytes());
        producer.send(message);
    }
}
