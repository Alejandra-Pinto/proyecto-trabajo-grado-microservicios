package co.unicauca.degreework.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Como CONSUMER, escuchas desde el exchange de DEGREEWORK
    private static final String DEGREEWORK_EXCHANGE = "degreework.exchange";
    private static final String EVALUATION_QUEUE = "evaluation.queue";
    private static final String EVALUATION_ROUTING_KEY = "degreework.evaluation";

    @Bean
    public TopicExchange degreeworkExchange() {
        return new TopicExchange(DEGREEWORK_EXCHANGE);
    }

    @Bean
    public Queue evaluationQueue() {
        return new Queue(EVALUATION_QUEUE, true);
    }

    @Bean
    public Binding evaluationBinding() {
        return BindingBuilder.bind(evaluationQueue())
                .to(degreeworkExchange())
                .with(EVALUATION_ROUTING_KEY);
    }
}