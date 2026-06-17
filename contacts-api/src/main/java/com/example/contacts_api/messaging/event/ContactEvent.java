package com.example.contacts_api.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Evento pubblicato su RabbitMQ ad ogni operazione CRUD su Contact.
 * È un semplice POJO serializzabile in JSON.
 *
 * NOTA: questo oggetto è il "contratto" tra producer e consumer.
 * In un sistema reale andrebbe in un modulo condiviso (es. contacts-events-lib)
 * importato come dipendenza da entrambi i microservizi — qui per semplicità
 * lo duplichiamo nel processor (lo vedremo nello Step 14).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEvent {

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    private EventType eventType;
    private Long contactId;
    private String surname;
    private String name;
    private String phoneNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private String otherInfo;
    private LocalDateTime occurredAt;
}