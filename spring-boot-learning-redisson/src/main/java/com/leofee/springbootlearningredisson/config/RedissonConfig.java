package com.leofee.springbootlearningredisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnResource(resources = "redisson.yml")
    @Bean
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson.yml"));
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient multipleRedissonClient() {
        Config config = new Config();
        config.useSentinelServers().addSentinelAddress("redis://localhost:6379",
                "redis://localhost:6380", "redis://localhost:6381");
        return Redisson.create(config);
    }
}