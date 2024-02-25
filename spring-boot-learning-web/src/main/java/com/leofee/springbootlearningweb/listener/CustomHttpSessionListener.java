package com.leofee.springbootlearningweb.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author leofee
 */
@WebListener
public class CustomHttpSessionListener implements HttpSessionListener {

    public static final AtomicInteger onLineCount = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("session created:" + se.getSession());
        onLineCount.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("session destroyed:" + se.getSession());
        onLineCount.decrementAndGet();
    }
}
