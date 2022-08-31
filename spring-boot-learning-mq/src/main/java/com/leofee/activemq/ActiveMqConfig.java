package com.leofee.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * @author leofee
 */
@EnableJms
@Configuration
public class ActiveMqConfig {

    /**
     * 基于 Queue 模式的
     *
     * @param jmsConnectionFactory 连接工厂
     * @return
     */
    @Bean
    public JmsListenerContainerFactory<?> queue(ActiveMQConnectionFactory jmsConnectionFactory) {
        DefaultJmsListenerContainerFactory queueContainer = new DefaultJmsListenerContainerFactory();
        queueContainer.setConnectionFactory(jmsConnectionFactory);
        return queueContainer;
    }

    /**
     * 基于 Topic 模式的
     *
     * @param jmsConnectionFactory 连接工厂
     * @return
     */
    @Bean
    public JmsListenerContainerFactory<?> topic(ConnectionFactory jmsConnectionFactory) {
        DefaultJmsListenerContainerFactory queueContainer = new DefaultJmsListenerContainerFactory();
        queueContainer.setConnectionFactory(jmsConnectionFactory);
        // 自定义同时开启pub_sub和点对点模式，因为activeMQ 默认只支持一种模式
        queueContainer.setPubSubDomain(true);
        return queueContainer;
    }
}
