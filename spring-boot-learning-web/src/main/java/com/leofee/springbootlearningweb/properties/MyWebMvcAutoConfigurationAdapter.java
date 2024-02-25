package com.leofee.springbootlearningweb.properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author leofee
 */
@Configuration
public class MyWebMvcAutoConfigurationAdapter implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceHandlerRegistration registration = registry.addResourceHandler("/custom/**");
        registration.addResourceLocations("classpath:/custom/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/static/*", "/custom/Hello.html");
        registry.addViewController("/view/**").setViewName("/custom/View.html");
    }
}
