package com.leofee.springbootlearningweb.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author leofee
 */
@Component
@ConfigurationProperties(prefix = "com.person")
@Data
public class PersonProperties {
    private String name;

    private String sex;

    private Integer age;

    private String companyName;

    private Car car = new Car();

    @Data
    public static class Car {
        private String carName;
    }
}
