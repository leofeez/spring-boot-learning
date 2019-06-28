package com.leofee.springbootlearningredis.persist.dao;

import com.leofee.springbootlearningredis.persist.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDao extends JpaRepository<Person, Long> {
}
