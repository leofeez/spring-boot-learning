package com.leofee.springbootlearningredisson.controller;


import com.leofee.springbootlearningredisson.persist.dao.PersonDao;
import com.leofee.springbootlearningredisson.persist.entity.Person;
import com.leofee.springbootlearningredisson.utils.RedissonCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @Autowired
    private RedissonCacheUtils redissonUtil;

    private static final String CACHE_KEY_PERSON = "person:person:";

    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        return redissonUtil.getCachedObjectOrElseGet(
                CACHE_KEY_PERSON.concat(personId.toString()),
                () -> personDao.findById(personId).orElseGet(Person::new));
    }

    @RequestMapping("/getPersonName")
    public String getPersonName(Long personId) {
        String personname = redissonUtil.getCachedObjectOrElseGet(
                CACHE_KEY_PERSON.concat(personId.toString()),
                () -> personDao.findById(personId).map(Person::getName).orElse(""));
        System.out.println("personName:" + personname);
        return personname;
    }

    @RequestMapping("/getPersonList")
    public List<Person> getPersonList(String name) {

        Person condition = new Person();
        condition.setName(name);

        return redissonUtil.getCachedListOrElseGet(
                CACHE_KEY_PERSON.concat(name),
                () -> personDao.findByName(name));
    }

    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
        redissonUtil.cacheObject(CACHE_KEY_PERSON.concat(person.getId().toString()), person);
    }

    @RequestMapping("/deletePerson")
    public void deletePerson(Long personId) {
        personDao.deleteById(personId);
    }

    @RequestMapping("/deleteAll")
    public void deletePerson() {
        personDao.deleteAll();
    }
}
