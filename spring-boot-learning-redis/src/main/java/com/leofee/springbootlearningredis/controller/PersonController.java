package com.leofee.springbootlearningredis.controller;


import com.leofee.springbootlearningredis.persist.dao.PersonDao;
import com.leofee.springbootlearningredis.persist.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@CacheConfig(cacheNames = "person")
@Slf4j
@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Cacheable(key = "'person_' + #personId ")
    @RequestMapping("/getPerson")
    public Object getPerson(Long personId) {
        // 1. 查询缓存
        Object cache = redisTemplate.opsForValue().get("person::" + personId);
        if (cache != null) {
            // 缓存命中直接返回
            return cache;
        }

        // 2. 缓存未命中，设置互斥锁并设置锁过期时间，防止死锁
        boolean success = redisTemplate.opsForValue().setIfAbsent("lock:data", "lock", 1000, TimeUnit.MILLISECONDS);
        if (success) {
            // 获取锁成功，查询数据库并
            Object data = personDao.findById(personId);
            redisTemplate.opsForValue().set("person::" + personId, data);
            return data;
        } else {
            // 获取锁失败，休眠后重新查询缓存
            Thread.sleep(100);
            return getPerson(personId);
        }
    }

    @CachePut(key = "'person_' + #person.id", value = "#person")
    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
    }

    @CacheEvict(key = "'person_' + #personId")
    @RequestMapping("/deletePerson")
    public void deletePerson(Long personId) {
        personDao.deleteById(personId);
    }

    @CacheEvict(allEntries = true)
    @RequestMapping("/deleteAll")
    public void deletePerson() {
        personDao.deleteAll();
    }
}
