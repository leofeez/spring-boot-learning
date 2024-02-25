package com.leofee.springbootlearningweb.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.Serializable;

/**
 * 自定义的 application.properties
 *
 * <p>
 * 自定义的配置项Spring boot 是不会自动读取并加载到容器中, 这种情况下必须手动通过
 * <code>@PropertySource("classpath:myApplication.properties")</code> 注解的方式指定配置文件的路径,
 * 并且需要在对应绑定的 java bean 上加上 <code>@Configuration</code> 表明这是一个配置, 这样 Spring boot
 * 会自动实例化该配置对象, 然后我们在其他地方直接注入该对象即可使用对应的配置,
 * 例如 {@link com.leofee.springbootlearningweb.controller.HelloWorldController}
 *
 * <p>
 * 在注入配置的对象的地方, 类上必须加上 <code>@EnableConfigurationProperties</code>
 *
 * @author leofee
 * @date 2019/6/26
 */
@PropertySource(value = { "classpath:db.yml", "classpath:person.properties"})
@Configuration
@Data
public class PropertiesConfiguration implements Serializable {
}
