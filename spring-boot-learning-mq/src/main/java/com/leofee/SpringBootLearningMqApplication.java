package com.leofee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@PropertySource("classpath:mq.yml")
@ServletComponentScan
@EnableWebMvc
@SpringBootApplication
public class SpringBootLearningMqApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLearningMqApplication.class, args);
    }

}
