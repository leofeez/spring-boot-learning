package com.leofee.activemq.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author leofee
 */
@Component
public class ActiveMqReceiver {

    @JmsListener(destination = "springboot_queue", containerFactory = "queue")
    public void receive(String msg) {
        System.out.println("接收到消息：" + msg);
    }

    @JmsListener(destination = "springboot_topic", containerFactory = "topic")
    public void receiveTopic(String msg) {
        System.out.println("接收到消息Topic：" + msg);
    }
}
