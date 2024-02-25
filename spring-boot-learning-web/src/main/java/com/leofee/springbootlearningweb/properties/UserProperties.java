package com.leofee.springbootlearningweb.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自动加载 properties 中的配置信息
 *
 * 默认的 application.properties 中的配置是可以自动被Spring boot 加载到容器中,
 * 一般情况下, 都是将这些配置项与对应的 java bean 绑定起来, 例如 {@link UserProperties}.
 *
 *
 * @author leofee
 * @date 2019/6/26
 */
@Data
@Component
public class UserProperties implements Serializable {

    @Value(value = "${com.user.name}")
    private String name;

    @Value(value = "${com.user.sex}")
    private String sex;

    @Value(value = "#{${com.user.age} + 1}")
    private Integer age;
}
