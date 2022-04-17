package com.leofee.springbootlearningweb.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 利用注解的方式注册 Filter, 这种方式无法指定Filter 的执行顺序
 * 只能通过 Filter 的首字母进行排序
 *
 *
 *  <li>1.在 Filter 上增加注解 <code>@WebFilter(filterName = "helloWorldFilter", urlPatterns = "/*")</code>
 *  <li>2.在 启动类加上注解 <code>@ServletComponentScan</code>
 *
 * @author leofee
 * @date 2019/6/26
 */
@Slf4j
@WebFilter(filterName = "FirstHelloWorldFilter", urlPatterns = "/*")
public class FirstHelloWorldFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("filter:{} initial, initial parameter value:{}", getClass(), filterConfig.getInitParameter("name"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info(" before {} do filter, uri:{}", getClass(), request.getRequestURI());
        filterChain.doFilter(servletRequest, servletResponse);
        log.info("after {} do filter, uri:{}", getClass(), request.getRequestURI());
    }

    @Override
    public void destroy() {
        log.info("filter:{} destroy", getClass());
    }
}
