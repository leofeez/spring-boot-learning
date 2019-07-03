package com.leofee.springbootlearningredis.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class SessionController {


    @RequestMapping("getSession")
    public String getSession(HttpSession session) {
        Object username = session.getAttribute("username");
        if (username == null) {
            session.setAttribute("username", "leofee");
        }
        return session.getId();
    }
}
