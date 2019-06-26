package com.leofee.springbootlearningstarter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leofee
 * @date 2019/6/19
 */
@RestController
public class HelloWorld {

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
