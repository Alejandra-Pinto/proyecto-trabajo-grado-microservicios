package com.example.notification.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    //para usuarios, lo usamos para coordinadores:
    // === Cola de usuarios (FANOUT) ===
    @Value("${app.rabbitmq.users.exchange}")
    private String userExchangeName;

    @Value("${app.rabbitmq.users.queue}")
    private String userQueueName;

    @Value("${app.rabbitmq.users.exchange-type:fanout}")
    private String userExchangeType;

    

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(exchange).durable(true).build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue userQueue() {
        return new Queue(userQueueName, true);
    }

    @Bean
    public Exchange userExchange() {
        if ("fanout".equalsIgnoreCase(userExchangeType)) {
            return new FanoutExchange(userExchangeName);
        } else {
            return new DirectExchange(userExchangeName);
        }
    }

    @Bean
    public Binding userBinding(Queue userQueue, Exchange userExchange) {
        if (userExchange instanceof FanoutExchange) {
            return BindingBuilder.bind(userQueue).to((FanoutExchange) userExchange);
        }
        return null; // direct no aplica acá, pero lo dejamos seguro
    }

}
