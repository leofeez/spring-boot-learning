package com.leofee.springbootlearningstarter.controller;

import org.springframework.web.bind.annotation.RestController;

/**
 * @author leofee
 * @date 2019/6/19
 */
@RestController
public class HelloWorld {

    public String hello() {
        return "hello";
    }
}
