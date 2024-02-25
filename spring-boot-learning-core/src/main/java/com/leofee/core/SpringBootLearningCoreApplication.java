package com.leofee.core;

import com.leofee.core.listener.MyContextStartingListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author leofee
 */
@SpringBootApplication
public class SpringBootLearningCoreApplication {

    public static void main(String[] args) {
        SpringApplication ap = new SpringApplication(SpringBootLearningCoreApplication.class);
        ap.addListeners(new MyContextStartingListener());
        ap.run(args);
    }

}
