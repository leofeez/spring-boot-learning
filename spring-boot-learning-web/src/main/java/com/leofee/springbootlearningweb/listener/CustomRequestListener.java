package com.leofee.springbootlearningweb.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 * @author leofee
 */
@WebListener
public class CustomRequestListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        String remoteAddr = sre.getServletRequest().getRemoteAddr();
        System.out.println("remote address:" + remoteAddr + " 访问了系统。");
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {

    }
}
