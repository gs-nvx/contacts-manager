package com.example.contacts_api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.created}")
    private String createdRoutingKey;

    @Value("${app.rabbitmq.routing-key.updated}")
    private String updatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.deleted}")
    private String deletedRoutingKey;

    @Value("${app.rabbitmq.queue.created}")
    private String createdQueue;

    @Value("${app.rabbitmq.queue.updated}")
    private String updatedQueue;

    @Value("${app.rabbitmq.queue.deleted}")
    private String deletedQueue;

    // --- Exchange ---

    @Bean
    public TopicExchange contactsExchange() {
        return new TopicExchange(exchange);
    }

    // --- Code ---

    @Bean
    public Queue contactCreatedQueue() {
        return new Queue(createdQueue, true); // durable=true
    }

    @Bean
    public Queue contactUpdatedQueue() {
        return new Queue(updatedQueue, true);
    }

    @Bean
    public Queue contactDeletedQueue() {
        return new Queue(deletedQueue, true);
    }

    // --- Binding: collega ogni coda all'exchange tramite routing key ---

    @Bean
    public Binding createdBinding() {
        return BindingBuilder.bind(contactCreatedQueue())
                .to(contactsExchange())
                .with(createdRoutingKey);
    }

    @Bean
    public Binding updatedBinding() {
        return BindingBuilder.bind(contactUpdatedQueue())
                .to(contactsExchange())
                .with(updatedRoutingKey);
    }

    @Bean
    public Binding deletedBinding() {
        return BindingBuilder.bind(contactDeletedQueue())
                .to(contactsExchange())
                .with(deletedRoutingKey);
    }

    // --- Serializzazione JSON ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}