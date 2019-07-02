//package com.leofee.springbootlearningredisson.config;
//
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.client.codec.StringCodec;
//import org.redisson.codec.JsonJacksonCodec;
//import org.redisson.config.Config;
//import org.redisson.config.SingleServerConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.StringUtils;
//
///**
// * @author leofee
// */
//@Configuration
//@EnableConfigurationProperties(RedissonProperties.class)
//public class RedissonConfig {
//
//    @Autowired
//    private RedissonProperties redissonProperties;
//
//    /**
//     * 单机模式自动装配
//     *
//     * @return redisson client
//     */
//    @Bean(destroyMethod = "shutdown", name = "redissonSingle")
//    @ConditionalOnProperty(name = "redisson.address")
//    public RedissonClient redissonSingle() {
//        Config config = new Config();
//        SingleServerConfig serverConfig = config.useSingleServer()
//                .setAddress(redissonProperties.getAddress())
//                .setTimeout(redissonProperties.getTimeout())
//                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
//                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());
//
//        if (!StringUtils.isEmpty(redissonProperties.getPassword())) {
//            serverConfig.setPassword(redissonProperties.getPassword());
//        }
//        return Redisson.create(config);
//    }
//
//}