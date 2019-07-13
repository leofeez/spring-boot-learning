package com.leofee.springbootlearningredisson.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author leofee
 * @date 2019/7/12
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private RedissonClient redissonClient;


    public void createOrder() {
        String orderName = Thread.currentThread().getName();

        RBucket<Integer> count = redissonClient.getBucket("count");

        Integer value = count.get();

        if (value > 0) {
            log.info("订单：[{}]， 开始创建订单.....", orderName);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            --value;
            count.set(value);
            log.info("订单：[{}]， 创建成功！, 剩余库存[{}]", orderName, value);
        } else {
            log.info("订单：[{}], 创建失败，库存不足！", orderName);
        }
    }
}
