package com.leofee.springbootlearningredisson.controller;

import com.leofee.springbootlearningredisson.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonRedLock;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author leofee
 * @date 2019/7/11
 */
@Slf4j
@RestController
public class OrderController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedissonClient multipleRedissonClient;

    @Autowired
    private OrderService orderService;

    @RequestMapping("/orderByLock")
    public void orderByLock() {

        String orderName = Thread.currentThread().getName();
        String stockId = UUID.randomUUID().toString();

        RLock lock = redissonClient.getLock("stockLock" + stockId);

        RedissonRedLock redissonRedLock = new RedissonRedLock();

        boolean locked = false;

        try {

            log.info("订单：[{}]， 开始锁库存......", orderName);
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);

            if (locked) {
                orderService.createOrder();
            } else {
                log.info("[{}]订单生成超时......", orderName);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 只有持有锁的线程才能释放锁
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @RequestMapping("/orderNoLock")
    public void orderNoLock() {
        orderService.createOrder();
    }

    @RequestMapping("/initCount")
    public void initCount() {
        RBucket<Integer> count = redissonClient.getBucket("count");
        count.set(10);
    }
}
