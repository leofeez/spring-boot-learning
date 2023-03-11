package com.leofee.starter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author leofee
 */
@Configuration
public class MyStarterAutoConfiguration {

    @Bean
    public MyConfig myConfig() {
        System.out.println("==========my starter ============");
        return new MyConfig();
    }
}
