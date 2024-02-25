package com.leofee.springbootlearningweb.controller;

import com.leofee.springbootlearningweb.listener.CustomHttpSessionListener;
import com.leofee.springbootlearningweb.persist.entity.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * @author leofee
 */
@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login(HttpSession session, Model model) {
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String attrName = names.nextElement();
            System.out.println("session attr:" + attrName + "=" + session.getAttribute(attrName));
        }
        System.out.println("当前系统在线人数：" + CustomHttpSessionListener.onLineCount.intValue());

        Person person = new Person();
        person.setName("leofee");
        person.setAge(18);
        person.setSex("男");
        model.addAttribute("person", person);
        model.addAttribute("userCount", CustomHttpSessionListener.onLineCount.get());
        return "index";
    }

    @RequestMapping("/view")
    public String view() {
        return "hello world";
    }
}
