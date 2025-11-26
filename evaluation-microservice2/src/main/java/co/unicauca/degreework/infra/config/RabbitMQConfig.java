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

    /*@Bean
    public TopicExchange evaluationExchange() {
        return new TopicExchange(evaluationExchangeName);
    }*/
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
