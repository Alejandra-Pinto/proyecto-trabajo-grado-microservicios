package co.unicauca.degreework.infra.config;

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
    @Value("${app.rabbitmq.user.exchange}")
    private String userExchangeName;

    @Value("${app.rabbitmq.user.queue}")
    private String userQueueName;

    @Value("${app.rabbitmq.user.routingkey}")
    private String userRoutingKey;

    // === Cola de proyectos de grado ===
    @Value("${app.rabbitmq.degreework.exchange}")
    private String degreeworkExchangeName;

    @Value("${app.rabbitmq.degreework.queue}")
    private String degreeworkQueueName;

    @Value("${app.rabbitmq.degreework.routingkey}")
    private String degreeworkRoutingKey;

    // === Declaraciones para cola de usuarios ===
    @Bean
    public Queue userQueue() {
        return new Queue(userQueueName, true);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(userExchangeName);
    }

    @Bean
    public Binding userBinding(Queue userQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userQueue).to(userExchange).with(userRoutingKey);
    }

    // === Declaraciones para cola de proyectos de grado ===
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
