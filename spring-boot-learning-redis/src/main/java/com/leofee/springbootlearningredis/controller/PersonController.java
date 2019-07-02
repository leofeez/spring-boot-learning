package com.leofee.springbootlearningredis.controller;


import com.leofee.springbootlearningredis.persist.dao.PersonDao;
import com.leofee.springbootlearningredis.persist.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @Cacheable(key = "'person'+ #personId ",value = "person")
    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        Person person = new Person();
        person.setId(personId);
        Example<Person> condition = Example.of(person);
        return personDao.findOne(condition).orElse(null);
//        return personDao.findById(personId).orElseGet(Person::new);
    }

    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
    }

    @CacheEvict(key = "'person' + #personId")
    @RequestMapping("/deletePerson")
    public void deletePerson(Long personId) {
        personDao.deleteById(personId);
    }
}
