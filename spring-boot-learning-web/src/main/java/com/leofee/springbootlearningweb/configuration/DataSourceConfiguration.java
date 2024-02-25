package com.leofee.springbootlearningweb.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.leofee.springbootlearningweb.properties.YmlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author leofee
 */
@PropertySource(value = "classpath:db.yml", factory = YmlPropertySourceFactory.class)
@Configuration
public class DataSourceConfiguration {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DruidDataSource druidDatasource() {
        return new DruidDataSource();
    }
}
