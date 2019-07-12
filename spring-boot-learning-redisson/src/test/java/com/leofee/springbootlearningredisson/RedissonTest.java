package com.leofee.springbootlearningredisson;

import com.leofee.springbootlearningredisson.persist.entity.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

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
    public void setName() {
        RBucket<String> bucket = redissonClient.getBucket("name", new StringCodec());
        System.out.println(bucket.get());
        bucket.set("leofee");
    }


    @Test
    public void getName() {
        RBucket<String> bucket = redissonClient.getBucket("name", new StringCodec());
        System.out.println(bucket.get());
    }

    @Test
    public void getPerson() {
        RBucket bucket = redissonClient.getBucket("leofee");
        System.out.println(bucket.get());
    }

    @Test
    public void setPerson() {
        Person person = new Person();
        person.setName("leofee");
        person.setAge(20);
        RBucket<Person> bucket = redissonClient.getBucket("leofee");
        if (bucket.get() == null) {
            bucket.set(person);
        }
    }

    @Test
    public void setPersonList() {
        List<Person> personList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Person person = new Person();
            person.setName("leofee");
            person.setAge(20);
            personList.add(person);
        }


        RList<Person> personCache = redissonClient.getList("personList");
        personCache.addAll(personList);

    }

    @Test
    public void getPersonList() {
        RList<Person> bucket = redissonClient.getList("personList");
        List<Person> personList = bucket.readAll();
        for (Person person : personList) {
            System.out.println(person);
        }
    }


    @Test
    public void testLock() {

        RBucket<Integer> count = redissonClient.getBucket("count");
        count.set(10);
    }
}
