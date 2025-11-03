package com.example.evaluation.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String mainExchange;

    @Value("${app.rabbitmq.user.queue}")
    private String userQueue;

    @Value("${app.rabbitmq.user.routingkey}")
    private String userRoutingKey;

    @Value("${app.rabbitmq.degreework.queue}")
    private String degreeWorkQueue;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeWorkRoutingKey;

    // ===== Exchange principal (DirectExchange) =====
    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(mainExchange);
    }

    // ===== Cola para USER SERVICE (Evaluaciones) =====
    @Bean
    public Queue userQueue() {
        return new Queue(userQueue, true);
    }

    // ===== Cola para DEGREEWORK SERVICE (Asignación de evaluadores) =====
    @Bean
    public Queue degreeWorkQueue() {
        return new Queue(degreeWorkQueue, true);
    }

    // ===== Binding: user.queue -> evaluation.exchange con user.routingkey =====
    @Bean
    public Binding bindingUser(Queue userQueue, DirectExchange mainExchange) {
        return BindingBuilder.bind(userQueue).to(mainExchange).with(userRoutingKey);
    }

    // ===== Binding: degreework.queue -> evaluation.exchange con
    // degreework.routingkey =====
    @Bean
    public Binding bindingDegreeWork(Queue degreeWorkQueue, DirectExchange mainExchange) {
        return BindingBuilder.bind(degreeWorkQueue).to(mainExchange).with(degreeWorkRoutingKey);
    }

    // ===== Convertidor JSON para serializar DTOs =====
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}