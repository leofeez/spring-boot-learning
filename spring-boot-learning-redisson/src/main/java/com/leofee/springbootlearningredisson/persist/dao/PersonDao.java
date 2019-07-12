package com.leofee.springbootlearningredisson.persist.dao;

import com.leofee.springbootlearningredisson.persist.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDao extends JpaRepository<Person, Long> {
}
