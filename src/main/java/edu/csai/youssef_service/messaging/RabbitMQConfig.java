package edu.csai.youssef_service.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for WorkHub async workflows.
 *
 * Exchange : workhub.events   (topic, durable)
 * Queue    : workhub.tasks    (durable, DLQ-bound for reliability)
 * Dead-Letter Queue: workhub.tasks.dlq  (receives messages that fail after retries)
 *
 * Routing keys
 *   project.created  → workhub.tasks queue
 *   task.created     → workhub.tasks queue
 */
@Configuration
public class RabbitMQConfig {

    // ── Topology constants ────────────────────────────────────────────────

    public static final String EXCHANGE    = "workhub.events";
    public static final String QUEUE       = "workhub.tasks";
    public static final String DLQ         = "workhub.tasks.dlq";
    public static final String DLX         = "workhub.dead-letter";

    public static final String RK_PROJECT_CREATED = "project.created";
    public static final String RK_TASK_CREATED    = "task.created";

    // ── Exchange declarations ─────────────────────────────────────────────

    @Bean
    public TopicExchange workhubExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ── Queue declarations ────────────────────────────────────────────────

    /** Main work queue — messages that exhaust retries go to DLQ. */
    @Bean
    public Queue workhubTasksQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    /** Dead-letter queue for failed messages. */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────

    @Bean
    public Binding bindProjectCreated(Queue workhubTasksQueue, TopicExchange workhubExchange) {
        return BindingBuilder.bind(workhubTasksQueue).to(workhubExchange).with(RK_PROJECT_CREATED);
    }

    @Bean
    public Binding bindTaskCreated(Queue workhubTasksQueue, TopicExchange workhubExchange) {
        return BindingBuilder.bind(workhubTasksQueue).to(workhubExchange).with(RK_TASK_CREATED);
    }

    @Bean
    public Binding bindDlq(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ);
    }

    // ── Serialisation ─────────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /** Consumer container: max 3 retries before DLQ, one thread per queue. */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        return factory;
    }
}
