package com.example.users.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.users.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    // El routing key ahora es opcional para fanout
    @Value("${app.rabbitmq.users.routingkey:}")
    private String routingKey;

    @Value("${app.rabbitmq.users.exchange-type:fanout}")
    private String exchangeType;

    @Bean
    public Exchange exchange() {
        System.out.println("Configurando Exchange: " + exchange + " tipo: " + exchangeType);
        
        if ("fanout".equalsIgnoreCase(exchangeType)) {
            return ExchangeBuilder.fanoutExchange(exchange).durable(true).build();
        } else {
            return ExchangeBuilder.directExchange(exchange).durable(true).build();
        }
    }

    @Bean
    public Queue queue() {
        System.out.println("Configurando Queue: " + queue);
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        System.out.println("Configurando Binding para Fanout Exchange");
        
        if (exchange instanceof FanoutExchange) {
            // Para Fanout, no se necesita routing key
            return BindingBuilder.bind(queue).to((FanoutExchange) exchange);
        } else {
            // Para Direct/Topic, mantener routing key
            return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        }
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}