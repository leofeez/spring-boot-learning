package com.leofee.springbootlearningredis;

import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author leofee
 * @date 2019/7/2
 */
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedissonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    @Test
    public void getName() {
        redissonClient.getConfig().setCodec(new StringCodec());
        RBucket<String> bucket = redissonClient.getBucket("name", new StringCodec());
        System.out.println(bucket.get());
    }

    @Test
    public void getPerson() {
        JsonJacksonCodec jsonJacksonCodec = new JsonJacksonCodec();

        redissonClient.getConfig().setCodec(jsonJacksonCodec);
        RBucket bucket = redissonClient.getBucket("leofee", jsonJacksonCodec);
        System.out.println(bucket.get());
    }

    @Test
    public void getPersonFromDb() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/getPerson")
                .contentType(MediaType.APPLICATION_JSON)
                .param("personId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("小明"));
    }

    @Test
    public void testLock() {

        // 向缓存中添加一个key
        redisTemplate.opsForValue().set("count", 100);




        ExecutorService executorService = Executors.newFixedThreadPool(200);

        for (int i = 0; i < 200; i++) {
            executorService.execute(new PersonThread("lock_thread_" + (i + 1)));
        }

    }

    @AllArgsConstructor
    private class PersonThread implements Runnable {

        private String threadName;

        @Override
        public void run() {

            RLock lock = redissonClient.getLock("personLock");

            boolean result = false;

            try {

                result = lock.tryLock(20, 10, TimeUnit.SECONDS);
                if (result) {
                    System.out.println(threadName + "_开始处理......");

                    Integer count = redisTemplate.opsForValue().get("count");

                    if (count != null && count > 0) {

                        redisTemplate.opsForValue().set("count", --count);

                        System.out.println(threadName + "_处理结束......count 剩余:" + count);

                    } else {
                        System.out.println(threadName + "_处理失败......count 剩余不足");
                    }


                } else {
                    System.out.println(threadName + "_等待超时......");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (result) {
                    System.out.println(threadName + "_释放锁......");
                    lock.unlock();
                }
            }
        }
    }
}
