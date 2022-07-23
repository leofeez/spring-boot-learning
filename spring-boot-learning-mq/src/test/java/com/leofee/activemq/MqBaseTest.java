package com.leofee.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;

/**
 * @author leofee
 */
public class MqBaseTest {

    protected ActiveMQConnectionFactory activeMQConnectionFactory;

    @Before
    public void initActiveMQConnectionFactory() {
        // 默认的 broker url 为 tcp://localhost:61616
        this.activeMQConnectionFactory = new ActiveMQConnectionFactory("admin", "admin123", ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
    }
}