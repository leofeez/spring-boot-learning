package com.leofee.rocketmq.controller;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leofee
 */
@RequestMapping("/rocketmq")
@RestController
public class RocketMqController {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @RequestMapping("/send")
    public String send() {

        rocketMQTemplate.syncSend("leofee_topic", "hello rocket mq");

        return "send success";
    }
}
