package com.example.contacts_search_processor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    /**
     * Senza questo bean, Spring AMQP usa un container factory di default
     * con converter binario — il MessageConverter JSON dichiarato sopra
     * NON viene applicato automaticamente ai @RabbitListener.
     * Questo factory esplicito collega i due.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @Bean
    public RabbitListenerErrorHandler contactEventErrorHandler() {
        return (amqpMessage, channel, message, exception) -> {
            log.error("Errore elaborazione evento RabbitMQ sul messaggio {}: {}",
                    amqpMessage, exception.getCause() != null
                            ? exception.getCause().getMessage()
                            : exception.getMessage());
            return null;
        };
    }

}