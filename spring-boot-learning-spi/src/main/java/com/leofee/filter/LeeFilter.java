package com.leofee.filter;

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
 * @author leofee
 * @date 2019/9/27
 */
@WebFilter(urlPatterns = "/")
public class LeeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(getClass() + " 过滤器 初始化!");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(getClass() + " 过滤器 开始过滤请求! uri 为" + httpServletRequest.getRequestURI());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println(getClass() + " 过滤器 摧毁!");
    }
}
