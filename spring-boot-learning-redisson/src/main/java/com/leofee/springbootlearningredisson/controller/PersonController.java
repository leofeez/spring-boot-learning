package com.leofee.springbootlearningredisson.controller;


import com.leofee.springbootlearningredisson.RedissonHelper;
import com.leofee.springbootlearningredisson.persist.dao.PersonDao;
import com.leofee.springbootlearningredisson.persist.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @Autowired
    private RedissonHelper redissonHelper;

    private static final String CACHE_KEY_PERSON = "person::person_";

    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        return redissonHelper.getCachedObjectOrElseFromDB(
                CACHE_KEY_PERSON.concat(personId.toString()),
                Person.class,
                personId,
                result -> personDao.findById(personId).orElseGet(Person::new));
    }

    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
        redissonHelper.cacheObject(CACHE_KEY_PERSON.concat(person.getId().toString()), person);
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
