package com.leofee.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;

/**
 * @author leofee
 */
public class ActiveMqBaseTest {

    protected ActiveMQConnectionFactory activeMQConnectionFactory;

    @Before
    public void initActiveMQConnectionFactory() {
        // 默认的 broker url 为 tcp://localhost:61616
        // 远程broker url 为 "tcp://192.168.248.131:61616"
        this.activeMQConnectionFactory = new ActiveMQConnectionFactory("admin", "admin", "nio://192.168.248.131:61618");
        this.activeMQConnectionFactory.setTrustAllPackages(true);
    }
}
