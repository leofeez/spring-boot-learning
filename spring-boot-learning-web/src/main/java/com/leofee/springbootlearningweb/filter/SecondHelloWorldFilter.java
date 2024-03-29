package com.leofee.springbootlearningweb.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 利用注册 FilterRegistrationBean 方式注册 Filter
 *
 * @author leofee
 * @date 2019/6/26
 */
@Slf4j
public class SecondHelloWorldFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("filter:{} initial, initial parameter value:{}", getClass(), filterConfig.getInitParameter("name"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info("before {} do filter, uri:{}", getClass(), request.getRequestURI());
        filterChain.doFilter(servletRequest, servletResponse);
        log.info("after {} do filter, uri:{}", getClass(), request.getRequestURI());
    }

    @Override
    public void destroy() {
        log.info("filter:{} destroy", getClass());
    }
}
