package com.leofee.springbootlearningweb.persist.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String sex;

    @Column
    private Integer age;

}
