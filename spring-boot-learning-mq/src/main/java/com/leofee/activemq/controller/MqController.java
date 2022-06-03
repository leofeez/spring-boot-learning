package com.leofee.activemq.controller;


import com.leofee.activemq.sender.ActiveMqSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqController {

    @Autowired
    private ActiveMqSender sender;

    @Autowired
    private Environment environment;


    @GetMapping("/send")
    public String send() {

        sender.send("leofee_queue", "hello");


        String property = environment.getProperty("spring.activemq.password");

        return "ok";
    }
}
