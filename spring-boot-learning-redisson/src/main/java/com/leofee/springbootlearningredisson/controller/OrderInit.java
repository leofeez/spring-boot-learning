package com.leofee.springbootlearningredisson.controller;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author leofee
 * @date 2019/7/12
 */
@Component
public class OrderInit implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // 容器启动时往redis中设置count = 10
        RedissonClient redissonClient = applicationContext.getBean(RedissonClient.class);
        RBucket<Integer> count = redissonClient.getBucket("count");
        count.set(10);
    }
}
