package com.leofee.rocketmq.controller.listener;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author leofee
 */
@Component
@RocketMQMessageListener(topic = "leofee_topic", consumerGroup = "leofee_consumer")
public class MyRocketMqListener implements RocketMQListener<String> {


    @Override
    public void onMessage(String s) {
        System.out.println("received message:" + s);
    }
}
