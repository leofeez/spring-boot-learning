package com.leofee.springbootlearningweb.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.leofee.springbootlearningweb.persist.entity.Person;
import com.leofee.springbootlearningweb.properties.PropertiesConfiguration;
import com.leofee.springbootlearningweb.properties.PersonProperties;
import com.leofee.springbootlearningweb.properties.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leofee
 * @date 2019/6/26
 */
@RestController
public class HelloWorldController {

    @Autowired
    private UserProperties user;

    @Autowired
    private PersonProperties personProperties;

    @Autowired
    private DruidDataSource dataSource;

    @RequestMapping("/hello")
    public String hello() {
        System.out.println("当前数据源initialSize：" + dataSource.getInitialSize());
        return "hello world ! my name is " + user.getName() + ", person: " + personProperties.toString();
    }
}
