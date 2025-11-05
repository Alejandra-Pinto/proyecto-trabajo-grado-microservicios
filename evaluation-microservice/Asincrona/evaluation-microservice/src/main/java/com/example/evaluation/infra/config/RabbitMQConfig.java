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

    @Value("${app.rabbitmq.evaluation.exchange}")
    private String mainExchange;

    @Value("${app.rabbitmq.user.queue}")
    private String userQueue;

    @Value("${app.rabbitmq.user.routingkey}")
    private String userRoutingKey;

    @Value("${app.rabbitmq.degreework.queue}")
    private String degreeWorkQueue;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeWorkRoutingKey;

    // Colas y routing keys para notificaciones (nuevas)
    @Value("${app.rabbitmq.notification.exchange}")
    private String notificationExchange;

    @Value("${app.rabbitmq.notification.queue}")
    private String notificationQueue;

    @Value("${app.rabbitmq.notification.routingkey}")
    private String notificationRoutingKey;

    // ===== Exchange principal (DirectExchange) =====
    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(mainExchange);
    }

    // ===== Exchange para notificaciones (DirectExchange) =====
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(notificationExchange);
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

    // ===== Cola para NOTIFICACIONES =====
    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    // ===== Binding: user.queue -> evaluation.exchange con user.routingkey =====
    @Bean
    public Binding bindingUser(Queue userQueue, DirectExchange mainExchange) {
        return BindingBuilder.bind(userQueue).to(mainExchange).with(userRoutingKey);
    }

    // ===== Binding: degreework.queue -> evaluation.exchange con degreework.routingkey =====
    @Bean
    public Binding bindingDegreeWork(Queue degreeWorkQueue, DirectExchange mainExchange) {
        return BindingBuilder.bind(degreeWorkQueue).to(mainExchange).with(degreeWorkRoutingKey);
    }

    // ===== Binding: notification.queue -> notification.exchange con notification.routingkey =====
    @Bean
    public Binding bindingNotification(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(notificationRoutingKey);
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