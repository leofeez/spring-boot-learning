package com.leofee.springbootlearningredisson.persist.dao;

import com.leofee.springbootlearningredisson.persist.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonDao extends JpaRepository<Person, Long> {

    List<Person> findByName(String name);
}
