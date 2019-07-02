package com.leofee.springbootlearningredis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
}
