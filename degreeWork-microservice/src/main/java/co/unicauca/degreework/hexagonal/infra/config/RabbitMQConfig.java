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

    // === NUEVAS COLAS para evaluación (estados y evaluadores) ===
    @Value("${app.rabbitmq.evaluation.queue.status}")
    private String statusQueueName;
    
    @Value("${app.rabbitmq.evaluation.queue.evaluators}")
    private String evaluatorsQueueName;

    // === Exchange para evaluación (opcional, si decides usarlo más adelante) ===
    @Value("${app.rabbitmq.evaluation.exchange:evaluation.exchange}")
    private String evaluationExchangeName;
    
    @Value("${app.rabbitmq.evaluation.routingkey.status:degreework.status}")
    private String statusRoutingKey;
    
    @Value("${app.rabbitmq.evaluation.routingkey.evaluators:degreework.evaluators}")
    private String evaluatorsRoutingKey;

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
            return BindingBuilder.bind(userQueue).to((FanoutExchange) userExchange);
        } else {
            return BindingBuilder.bind(userQueue).to((DirectExchange) userExchange).with(userRoutingKey);
        }
    }

    // === Declaraciones para cola de proyectos de grado (DIRECT) ===
    @Bean
    public Queue degreeworkQueue() {
        System.out.println("🔧 Creando cola de proyectos de grado: " + degreeworkQueueName);
        return new Queue(degreeworkQueueName, true);
    }

    @Bean
    public DirectExchange degreeworkExchange() {
        System.out.println("🔧 Creando exchange de proyectos de grado: " + degreeworkExchangeName);
        return new DirectExchange(degreeworkExchangeName);
    }

    @Bean
    public Binding degreeworkBinding(Queue degreeworkQueue, DirectExchange degreeworkExchange) {
        System.out.println("🔧 Vinculando cola de proyectos de grado con routing key: " + degreeworkRoutingKey);
        return BindingBuilder.bind(degreeworkQueue).to(degreeworkExchange).with(degreeworkRoutingKey);
    }

    // === Declaraciones para cola de notificaciones (DIRECT) ===
    @Bean
    public Queue notificationQueue() {
        System.out.println("🔧 Creando cola de notificaciones: " + notificationQueueName);
        return new Queue(notificationQueueName, true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        System.out.println("🔧 Creando exchange de notificaciones: " + notificationExchangeName);
        return new DirectExchange(notificationExchangeName);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        System.out.println("🔧 Vinculando cola de notificaciones con routing key: " + notificationRoutingKey);
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(notificationRoutingKey);
    }

    // === NUEVAS COLAS para estados y evaluadores (DIRECT) ===
    @Bean
    public Queue statusQueue() {
        System.out.println("🔧 Creando cola de estados: " + statusQueueName);
        return new Queue(statusQueueName, true);
    }

    @Bean
    public Queue evaluatorsQueue() {
        System.out.println("🔧 Creando cola de evaluadores: " + evaluatorsQueueName);
        return new Queue(evaluatorsQueueName, true);
    }

    /**
     * OPCIÓN 1: Usando Direct Exchange (recomendado si quieres usar exchange)
     * Si prefieres enviar directamente a las colas, no necesitas este exchange
     */
    @Bean
    public DirectExchange evaluationExchange() {
        System.out.println("🔧 Creando exchange de evaluación: " + evaluationExchangeName);
        return new DirectExchange(evaluationExchangeName);
    }

    @Bean
    public Binding statusBinding(Queue statusQueue, DirectExchange evaluationExchange) {
        System.out.println("🔧 Vinculando cola de estados con routing key: " + statusRoutingKey);
        return BindingBuilder.bind(statusQueue).to(evaluationExchange).with(statusRoutingKey);
    }

    @Bean
    public Binding evaluatorsBinding(Queue evaluatorsQueue, DirectExchange evaluationExchange) {
        System.out.println("🔧 Vinculando cola de evaluadores con routing key: " + evaluatorsRoutingKey);
        return BindingBuilder.bind(evaluatorsQueue).to(evaluationExchange).with(evaluatorsRoutingKey);
    }

    // === Conversor JSON ===
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // === RabbitTemplate ===
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // Configuraciones adicionales para mejor manejo de errores
        template.setMandatory(true);
        template.setReplyTimeout(60000);
        
        return template;
    }
}