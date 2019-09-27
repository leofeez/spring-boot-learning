package com.leofee.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义的 servlet
 *
 * @author leofee
 * @date 2019/9/27
 */
@WebServlet(urlPatterns = "/")
public class LeeServlet extends HttpServlet {

    /**
     * 重写父类的 {@code doGet} 方法
     *
     * @param req  request
     * @param resp response
     * @throws IOException exception
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("hello leofee! this is my servlet!");
    }
}
