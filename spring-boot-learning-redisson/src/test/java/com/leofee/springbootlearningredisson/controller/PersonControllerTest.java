package com.leofee.springbootlearningredisson.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getPerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/getPerson")
                .contentType(MediaType.APPLICATION_JSON)
                .param("personId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("小明"));
    }

    @Test
    public void savePerson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/savePerson")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"leofee\",\"sex\":\"male\",\"age\":25}"))
                .andExpect(status().isOk());
    }

    @Test
    public void deletePerson() {
    }

    @Test
    public void deletePerson1() {
    }
}