package com.leofee.springbootlearningweb.controller;

import com.leofee.springbootlearningweb.persist.dao.PersonDao;
import com.leofee.springbootlearningweb.persist.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PersonController {

    @Autowired
    private PersonDao personDao;

    @RequestMapping("/queryPersonList")
    public List<Person> getPerson() {
        return personDao.findAll();
    }

    @RequestMapping("/getPerson")
    public Person getPerson(Long personId) {
        return personDao.findById(personId).orElseGet(Person::new);
    }

    @RequestMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personDao.save(person);
    }
}
