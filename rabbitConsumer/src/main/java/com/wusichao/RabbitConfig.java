package com.wusichao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitConfig {
    @Value("${spring.rabbitmq.host}")
    private String addresses;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${spring.rabbitmq.publisher-confirms}")
    private boolean publisherConfirms;

    @Bean
    public ConnectionFactory connectionFactory() {

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(addresses+":"+port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        /** 如果要进行消息回调，则这里必须要设置为true */
        connectionFactory.setPublisherConfirms(publisherConfirms);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate newRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("发送者确认发送给mq成功");
            } else {
                //处理失败的消息
               log.info("发送者发送给mq失败,考虑重发 cause: {}",cause);
            }
        });
        template.setReturnCallback((message, i, replyText, exchange, routingKey) -> {
            log.info("无法路由的消息，需要考虑另外处理。");
            log.info("Returned replyText：{}", replyText);
            log.info("Returned exchange：{}", exchange);
            log.info("Returned routingKey：{}", routingKey);
            String msgJson  = new String(message.getBody());
            log.info("Returned Message：{}", msgJson);
        });
        return template;
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("order.direct");
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("order.fanout");
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("order.topic");
    }

    @Bean
    public Queue orderCreate() {
        return new Queue("order.create");
    }

    @Bean
    public Queue orderCreateAndUpdate() {
        return new Queue("order.createAndUpdate");
    }

    @Bean
    public Binding orderCreateBindingOrderDirect() {
        return BindingBuilder.bind(orderCreate()).to(directExchange()).with("create");
    }

    @Bean
    public Binding orderCreateBindingOrderFanout() {
        return BindingBuilder.bind(orderCreate()).to(fanoutExchange());
    }

    @Bean
    public Binding orderCreateBindingOrderTopic() {
        return BindingBuilder.bind(orderCreateAndUpdate()).to(topicExchange()).with("create");
    }

    @Bean
    public Binding orderCreateAndUpdateBindingOrderTopic() {
        return BindingBuilder.bind(orderCreateAndUpdate()).to(topicExchange()).with("#");
    }
}
