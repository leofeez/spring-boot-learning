package com.leofee.springbootlearningredisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * @author leofee
 */
@Configuration
public class RedissonConfig {

    /**
     * 单机模式自动装配
     *
     * @return redisson client
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(new File("redisson.yml"));
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }

}