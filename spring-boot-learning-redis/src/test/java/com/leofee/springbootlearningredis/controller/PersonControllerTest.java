package com.leofee.springbootlearningredis.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Sql("classpath:person.sql")
    @Transactional
    @Rollback
    @Test
    public void getPerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/getPerson")
                .contentType(MediaType.APPLICATION_JSON)
                .param("personId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("leofee"));
    }


    @Test
    public void savePerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/savePerson")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"leofee\",\"sex\":\"male\",\"age\":25}"))
                .andExpect(status().isOk());
    }

    @Test
    public void updatePerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/savePerson")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":5, \"name\":\"leofee\",\"sex\":\"male\",\"age\":25}"))
                .andExpect(status().isOk());
    }

    @Test
    public void deletePerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/deletePerson")
                .contentType(MediaType.APPLICATION_JSON)
                .param("personId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteAllPerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/deleteAll"))
                .andExpect(status().isOk());
    }
}