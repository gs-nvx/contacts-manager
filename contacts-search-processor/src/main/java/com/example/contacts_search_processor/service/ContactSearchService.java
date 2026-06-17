package com.example.contacts_search_processor.service;

import com.example.contacts_search_processor.document.ContactDocument;
import com.example.contacts_search_processor.messaging.event.ContactEvent;
import com.example.contacts_search_processor.repository.ContactSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactSearchService {

    private final ContactSearchRepository searchRepository;

    public void indexContact(ContactEvent event) {
        ContactDocument document = ContactDocument.builder()
                .id(String.valueOf(event.getContactId()))
                .surname(event.getSurname())
                .name(event.getName())
                .phoneNumber(event.getPhoneNumber())
                .address(event.getAddress())
                .location(buildGeoPoint(event))
                .otherInfo(event.getOtherInfo())
                .build();

        searchRepository.save(document);
        log.info("Contatto indicizzato su Elasticsearch: id={}", event.getContactId());
    }

    public void removeContact(Long contactId) {
        searchRepository.deleteById(String.valueOf(contactId));
        log.info("Contatto rimosso da Elasticsearch: id={}", contactId);
    }

    public Page<ContactDocument> search(String term, Pageable pageable) {
        return searchRepository.findByFullText(term, pageable);
    }

    public Page<ContactDocument> findByPhone(String phoneNumber, Pageable pageable) {
        return searchRepository.findByPhoneNumber(phoneNumber, pageable);
    }

    private GeoPoint buildGeoPoint(ContactEvent event) {
        if (event.getLatitude() == null || event.getLongitude() == null) {
            return null;
        }
        return new GeoPoint(event.getLatitude(), event.getLongitude());
    }
}