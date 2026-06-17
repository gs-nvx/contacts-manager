package com.example.contacts_search_processor.messaging;

import com.example.contacts_search_processor.messaging.event.ContactEvent;
import com.example.contacts_search_processor.service.ContactSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactEventConsumer {

    private final ContactSearchService searchService;

    @RabbitListener(queues = "${app.rabbitmq.queue.created}",
            errorHandler = "contactEventErrorHandler")
    public void onContactCreated(ContactEvent event) {
        log.info("Evento ricevuto: CREATED contactId={}", event.getContactId());
        searchService.indexContact(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.updated}",
            errorHandler = "contactEventErrorHandler")
    public void onContactUpdated(ContactEvent event) {
        log.info("Evento ricevuto: UPDATED contactId={}", event.getContactId());
        searchService.indexContact(event); // upsert: save() su ES aggiorna se l'id esiste già
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.deleted}",
            errorHandler = "contactEventErrorHandler")
    public void onContactDeleted(ContactEvent event) {
        log.info("Evento ricevuto: DELETED contactId={}", event.getContactId());
        searchService.removeContact(event.getContactId());
    }
}