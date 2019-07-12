package com.leofee.springbootlearningredisson.controller;


import com.leofee.springbootlearningredisson.persist.dao.PersonDao;
import com.leofee.springbootlearningredisson.persist.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CacheConfig(cacheNames = "person")
@Slf4j
@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @Cacheable(key = "'person_' + #personId ")
    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        Person person = new Person();
        person.setId(personId);
        Example<Person> condition = Example.of(person);
        log.info("缓存未命中， 执行数据库查询......");
        return personDao.findOne(condition).orElse(null);
//        return personDao.findById(personId).orElseGet(Person::new);
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
