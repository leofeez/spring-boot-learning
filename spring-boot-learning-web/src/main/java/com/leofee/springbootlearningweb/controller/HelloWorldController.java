package com.leofee.springbootlearningweb.controller;

import com.leofee.springbootlearningweb.configuration.MyPropertiesConfiguration;
import com.leofee.springbootlearningweb.properties.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leofee
 * @date 2019/6/26
 */
@EnableConfigurationProperties
@RestController
public class HelloWorldController {

    @Autowired
    private User user;

    @Autowired
    private MyPropertiesConfiguration myPropertiesConfiguration;

    @RequestMapping("/hello")
    public String hello() {
        return "hello world ! my name is " + user.getName();
    }

    @RequestMapping("/world")
    public String world() {
        return "hello world ! author is " + myPropertiesConfiguration.getAuthor();
    }
}
