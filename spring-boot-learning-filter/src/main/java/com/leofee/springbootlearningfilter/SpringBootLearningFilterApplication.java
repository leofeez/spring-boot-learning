package com.leofee.springbootlearningfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SpringBootLearningFilterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLearningFilterApplication.class, args);
    }

}
