package com.leofee.springbootlearningredis;

import com.leofee.springbootlearningredis.persist.entity.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author leofee
 * @date 2019/6/28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Person> redisTemplate;

    @Test
    public void set() {

        // 向 redis 存一个键值
        stringRedisTemplate.opsForValue().set("name", "leofee");
        Person person = new Person();
        person.setId(1L);
        person.setName("leofee");
        person.setAge(28);
        person.setSex("male");
        ValueOperations<String, Person> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("leofee", person);

        Assert.assertEquals("leofee", stringRedisTemplate.opsForValue().get("name"));
    }


    @Test
    public void getPerson() throws Exception {

        Person person = redisTemplate.opsForValue().get("person::person_5");

        System.out.println(person == null ? null : person.toString());
    }
}
