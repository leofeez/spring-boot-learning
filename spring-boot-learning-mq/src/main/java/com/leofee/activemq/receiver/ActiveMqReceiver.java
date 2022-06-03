package com.leofee.activemq.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author leofee
 */
@Component
public class ActiveMqReceiver {

    @JmsListener(destination = "leofee_queue", containerFactory = "queue")
    public void receive(String msg) {
        System.out.println("接收到消息：" + msg);
    }
}
