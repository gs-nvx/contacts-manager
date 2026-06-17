package com.example.contacts_search_processor.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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