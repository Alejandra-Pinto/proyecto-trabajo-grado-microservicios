package co.unicauca.degreework.hexagonal.infra.config;

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

    // === Cola de usuarios ===
    @Value("${app.rabbitmq.users.exchange}")
    private String userExchangeName;

    @Value("${app.rabbitmq.users.queue}")
    private String userQueueName;

    @Value("${app.rabbitmq.users.routingkey:}")
    private String userRoutingKey;

    @Value("${app.rabbitmq.users.exchange-type:fanout}")
    private String userExchangeType;

    // === Cola de proyectos de grado ===
    @Value("${app.rabbitmq.degreework.exchange}")
    private String degreeworkExchangeName;

    @Value("${app.rabbitmq.degreework.queue}")
    private String degreeworkQueueName;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeworkRoutingKey;

    // === Cola de notificaciones ===
    @Value("${app.rabbitmq.notification.exchange}")
    private String notificationExchangeName;

    @Value("${app.rabbitmq.notification.queue}")
    private String notificationQueueName;

    @Value("${app.rabbitmq.notification.routingkey}")
    private String notificationRoutingKey;

    // === Declaraciones para cola de usuarios (FANOUT) ===
    @Bean
    public Queue userQueue() {
        System.out.println("🔧 Creando cola de usuarios: " + userQueueName);
        return new Queue(userQueueName, true);
    }

    @Bean
    public Exchange userExchange() {
        System.out.println("🔧 Creando exchange de usuarios: " + userExchangeName + " tipo: " + userExchangeType);
        
        if ("fanout".equalsIgnoreCase(userExchangeType)) {
            return new FanoutExchange(userExchangeName);
        } else {
            return new DirectExchange(userExchangeName);
        }
    }

    @Bean
    public Binding userBinding(Queue userQueue, Exchange userExchange) {
        System.out.println("🔧 Vinculando cola de usuarios al exchange");
        
        if (userExchange instanceof FanoutExchange) {
            // Para Fanout Exchange
            return BindingBuilder.bind(userQueue).to((FanoutExchange) userExchange);
        } else {
            // Para Direct Exchange (compatibilidad)
            return BindingBuilder.bind(userQueue).to((DirectExchange) userExchange).with(userRoutingKey);
        }
    }

    // === Declaraciones para cola de proyectos de grado (DIRECT - SIN CAMBIOS) ===
    @Bean
    public Queue degreeworkQueue() {
        return new Queue(degreeworkQueueName, true);
    }

    @Bean
    public DirectExchange degreeworkExchange() {
        return new DirectExchange(degreeworkExchangeName);
    }

    @Bean
    public Binding degreeworkBinding(Queue degreeworkQueue, DirectExchange degreeworkExchange) {
        return BindingBuilder.bind(degreeworkQueue).to(degreeworkExchange).with(degreeworkRoutingKey);
    }

    // === Declaraciones para cola de notificaciones (DIRECT - SIN CAMBIOS) ===
    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueueName, true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(notificationExchangeName);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(notificationRoutingKey);
    }

    // === Conversor JSON (SIN CAMBIOS) ===
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // === RabbitTemplate (SIN CAMBIOS) ===
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}