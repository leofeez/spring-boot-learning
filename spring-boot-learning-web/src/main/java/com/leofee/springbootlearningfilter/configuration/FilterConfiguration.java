package com.leofee.springbootlearningfilter.configuration;

import com.leofee.springbootlearningfilter.filter.SecondHelloWorldFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author leofee
 * @date 2019/6/26
 */
@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<SecondHelloWorldFilter> getFirstHelloWorldFilter() {
        FilterRegistrationBean<SecondHelloWorldFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecondHelloWorldFilter());
        registrationBean.addInitParameter("name", "leofee");
        registrationBean.addUrlPatterns("/*");

        // 指定执行顺序
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
