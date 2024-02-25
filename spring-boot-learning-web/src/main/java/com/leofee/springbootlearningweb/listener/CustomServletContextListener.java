package com.leofee.springbootlearningweb.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Enumeration;

/**
 * @author leofee
 */
public class CustomServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String contextPath = sce.getServletContext().getContextPath();
        System.out.println(sce.getServletContext().getServletContextName() + " 初始化完成......");
        System.out.println(sce.getServletContext().getServletContextName() + " contextPath：" + contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(sce.getServletContext().getServletContextName() + " Servlet 销毁完成......");
    }
}
