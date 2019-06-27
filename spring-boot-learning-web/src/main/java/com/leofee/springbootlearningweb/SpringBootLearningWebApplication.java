package com.leofee.springbootlearningweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SpringBootLearningWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLearningWebApplication.class, args);
    }

}
