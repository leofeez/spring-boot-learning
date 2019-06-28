package com.leofee.springbootlearningredis.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis 配置类
 *
 * @author leofee
 * @date 2019/6/28
 */
@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 生成 redis 缓存的 key 的策略
     *
     * @return {@link KeyGenerator}
     */
    @Bean
    public KeyGenerator getKeyGenerator() {
        return (o, method, objects) -> {
            StringBuilder builder = new StringBuilder(o.getClass() + method.getName());
            if (objects != null) {

                for (Object object : objects) {
                    builder.append(object == null ? "" : object.toString());
                }
            }
            return builder.toString();
        };
    }


//    @Bean
//    public RedisTemplate redisTemplate() {
//
//    }
}
