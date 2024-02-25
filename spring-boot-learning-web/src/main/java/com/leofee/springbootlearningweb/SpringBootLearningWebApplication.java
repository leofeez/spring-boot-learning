package com.leofee.springbootlearningweb;

import com.leofee.springbootlearningweb.filter.SecondHelloWorldFilter;
import com.leofee.springbootlearningweb.listener.CustomServletContextListener;
import com.leofee.springbootlearningweb.servlet.SpringCustomServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties
@ServletComponentScan
@SpringBootApplication
public class SpringBootLearningWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLearningWebApplication.class, args);
    }

    /**
     * 通过ServletRegistrationBean 进行注册自定义的servlet
     */
    @Bean
    public ServletRegistrationBean<SpringCustomServlet> customServlet() {
        ServletRegistrationBean<SpringCustomServlet> servletRegistrationBean = new ServletRegistrationBean<>(new SpringCustomServlet());
        servletRegistrationBean.addUrlMappings("/customServlet");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    /**
     * 通过FilterRegistrationBean 进行注册自定义的 Listener
     */
    @Bean
    public FilterRegistrationBean<SecondHelloWorldFilter> customFilter() {
        FilterRegistrationBean<SecondHelloWorldFilter> registrationBean = new FilterRegistrationBean<>(new SecondHelloWorldFilter());
        registrationBean.addInitParameter("customFilter", "SecondHelloWorldFilter");
        registrationBean.addUrlPatterns("/listener/*");
        return registrationBean;
    }

    /**
     * 通过ServletListenerRegistrationBean 进行注册自定义的 Listener
     */
    @Bean
    public ServletListenerRegistrationBean<CustomServletContextListener> customListener() {
        ServletListenerRegistrationBean<CustomServletContextListener> registrationBean = new ServletListenerRegistrationBean<>(new CustomServletContextListener());
        return registrationBean;
    }
}
