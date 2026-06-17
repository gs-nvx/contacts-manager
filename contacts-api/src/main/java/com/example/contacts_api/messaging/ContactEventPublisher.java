package com.example.contacts_api.messaging;

import com.example.contacts_api.messaging.event.ContactEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.created}")
    private String createdKey;

    @Value("${app.rabbitmq.routing-key.updated}")
    private String updatedKey;

    @Value("${app.rabbitmq.routing-key.deleted}")
    private String deletedKey;

    public void publishCreated(ContactEvent event) {
        publish(event, createdKey);
    }

    public void publishUpdated(ContactEvent event) {
        publish(event, updatedKey);
    }

    public void publishDeleted(Long contactId) {
        ContactEvent event = ContactEvent.builder()
                .eventType(ContactEvent.EventType.DELETED)
                .contactId(contactId)
                .occurredAt(LocalDateTime.now())
                .build();
        publish(event, deletedKey);
    }

    private void publish(ContactEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Evento pubblicato: {} per contactId={}",
                    event.getEventType(), event.getContactId());
        } catch (Exception e) {
            // Log dell'errore ma NON propagare l'eccezione al chiamante:
            // un fallimento della messaggistica non deve far fallire
            // l'operazione CRUD principale (resilienza).
            log.error("Errore pubblicazione evento RabbitMQ per contactId={}: {}",
                    event.getContactId(), e.getMessage());
        }
    }
}