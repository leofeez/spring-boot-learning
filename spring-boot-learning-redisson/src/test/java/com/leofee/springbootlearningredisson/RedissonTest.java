package com.leofee.springbootlearningredisson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author leofee
 * @date 2019/7/2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

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
        System.out.println("");
    }

    @Test
    public void setPerson() {
        JsonJacksonCodec jsonJacksonCodec = new JsonJacksonCodec();

        redissonClient.getConfig().setCodec(jsonJacksonCodec);
        RBucket bucket = redissonClient.getBucket("leofee", jsonJacksonCodec);
        System.out.println(bucket.get());
        System.out.println("");
    }
}
