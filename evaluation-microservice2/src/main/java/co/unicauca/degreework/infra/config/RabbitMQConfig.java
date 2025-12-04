package co.unicauca.degreework.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitMQConfig {

    // ============================
    // ==  COLA DE USUARIOS (FANOUT) ==
    // ============================

    @Value("${app.rabbitmq.users.exchange}")
    private String usersExchangeName;

    @Value("${app.rabbitmq.users.queue}")
    private String usersQueueName;

    @Value("${app.rabbitmq.users.routingkey:}")
    private String usersRoutingKey;

    @Value("${app.rabbitmq.users.exchange-type:fanout}")
    private String usersExchangeType;

    
    // ============================
    // ==  NOTIFICACIONES  ==
    // ============================
    @Value("${app.rabbitmq.notification.exchange}")
    private String notificationExchangeName;

    @Value("${app.rabbitmq.notification.routingkey}")
    private String notificationRoutingKey;

    @Bean
    public Exchange usersExchange() {
        System.out.println("🔧 Creando exchange de usuarios: " + usersExchangeName + " tipo: " + usersExchangeType);
        
        if ("fanout".equalsIgnoreCase(usersExchangeType)) {
            return new FanoutExchange(usersExchangeName);
        } else {
            return new DirectExchange(usersExchangeName);
        }
    }

    @Bean
    public Queue usersQueue() {
        System.out.println("🔧 Creando cola de usuarios para evaluaciones: " + usersQueueName);
        return new Queue(usersQueueName, true);
    }

    @Bean
    public Binding usersBinding(Queue usersQueue, Exchange usersExchange) {
        System.out.println("🔧 Vinculando cola de usuarios al exchange fanout");
        
        if (usersExchange instanceof FanoutExchange) {
            return BindingBuilder.bind(usersQueue).to((FanoutExchange) usersExchange);
        } else {
            return BindingBuilder.bind(usersQueue).to((DirectExchange) usersExchange).with(usersRoutingKey);
        }
    }

    // ============================
    // ==  PROYECTOS DE GRADO   ==
    // ============================

    @Value("${app.rabbitmq.degreework.exchange}")
    private String degreeworkExchangeName;

    @Value("${app.rabbitmq.degreework.queue}")
    private String degreeworkQueueName;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeworkRoutingKey;

    @Bean
    public DirectExchange degreeworkExchange() {
        return new DirectExchange(degreeworkExchangeName);
    }

    @Bean
    public Queue degreeworkQueue() {
        return new Queue(degreeworkQueueName, true);
    }

    @Bean
    public Binding degreeworkBinding() {
        return BindingBuilder
                .bind(degreeworkQueue())
                .to(degreeworkExchange())
                .with(degreeworkRoutingKey);
    }

    // ============================
    // ==        EVALUACIÓN      ==
    // ============================

    @Value("${app.rabbitmq.evaluation.exchange}")
    private String evaluationExchangeName;

    @Value("${app.rabbitmq.evaluation.queue}")
    private String evaluationQueueName;

    @Value("${app.rabbitmq.evaluation.routingkey}")
    private String evaluationRoutingKey;

    @Bean
    public DirectExchange evaluationExchange() {
        return new DirectExchange(evaluationExchangeName);
    }

    @Bean
    public Queue evaluationQueue() {
        return new Queue(evaluationQueueName, true);
    }

    @Bean
    public Binding evaluationBinding() {
        return BindingBuilder
                .bind(evaluationQueue())
                .to(evaluationExchange())
                .with(evaluationRoutingKey);
    }

    // === Exchange de NOTIFICACIONES ===
    @Bean
    public DirectExchange notificationExchange() {
        System.out.println("🔧 [EVALUATION] Creando exchange de notificaciones: " + notificationExchangeName);
        return new DirectExchange(notificationExchangeName);
    }

    // === Conversor JSON ===
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // === RabbitTemplate (para enviar mensajes) ===
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}