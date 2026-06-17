package com.example.contacts_api.service.impl;

import com.example.contacts_api.dto.ContactRequestDto;
import com.example.contacts_api.dto.ContactResponseDto;
import com.example.contacts_api.entity.Contact;
import com.example.contacts_api.exception.ContactNotFoundException;
import com.example.contacts_api.exception.DuplicatePhoneNumberException;
import com.example.contacts_api.mapper.ContactMapper;
import com.example.contacts_api.messaging.ContactEventPublisher;
import com.example.contacts_api.messaging.event.ContactEvent;
import com.example.contacts_api.repository.ContactRepository;
import com.example.contacts_api.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactEventPublisher eventPublisher;

    @Override
    public ContactResponseDto create(ContactRequestDto request) {
        log.info("Creazione contatto: surname={}, name={}, phone={}",
                request.getSurname(), request.getName(), request.getPhoneNumber());

        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        log.debug("Numero normalizzato: {}", normalizedPhone);

        if (contactRepository.existsByPhoneNumber(normalizedPhone)) {
            log.warn("Tentativo di creazione con numero duplicato: {}", normalizedPhone);
            throw new DuplicatePhoneNumberException(normalizedPhone);
        }

        request.setPhoneNumber(normalizedPhone);
        Contact entity = ContactMapper.toEntity(request);
        Contact saved = contactRepository.save(entity);

        log.info("Contatto creato con id={}", saved.getId());

        eventPublisher.publishCreated(buildEvent(saved, ContactEvent.EventType.CREATED));
        log.debug("Evento CREATED pubblicato per id={}", saved.getId());

        return ContactMapper.toResponseDto(saved);
    }

    @Override
    public ContactResponseDto update(Long id, ContactRequestDto request) {
        log.info("Aggiornamento contatto id={}", id);

        Contact existing = contactRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Contatto non trovato per update: id={}", id);
                    return new ContactNotFoundException(id);
                });

        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        log.debug("Numero normalizzato: {}", normalizedPhone);

        if (!existing.getPhoneNumber().equals(normalizedPhone)
                && contactRepository.existsByPhoneNumber(normalizedPhone)) {
            log.warn("Numero di telefono già in uso da altro contatto: {}", normalizedPhone);
            throw new DuplicatePhoneNumberException(normalizedPhone);
        }

        request.setPhoneNumber(normalizedPhone);
        ContactMapper.updateEntity(existing, request);
        Contact saved = contactRepository.save(existing);

        log.info("Contatto aggiornato: id={}", saved.getId());

        eventPublisher.publishUpdated(buildEvent(saved, ContactEvent.EventType.UPDATED));
        log.debug("Evento UPDATED pubblicato per id={}", saved.getId());

        return ContactMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto getById(Long id) {
        log.debug("Recupero contatto id={}", id);

        Contact entity = contactRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Contatto non trovato: id={}", id);
                    return new ContactNotFoundException(id);
                });

        log.debug("Contatto trovato: id={}, surname={}", entity.getId(), entity.getSurname());
        return ContactMapper.toResponseDto(entity);
    }

    @Override
    public void delete(Long id) {
        log.info("Eliminazione contatto id={}", id);

        if (!contactRepository.existsById(id)) {
            log.warn("Tentativo di eliminazione su id inesistente: {}", id);
            throw new ContactNotFoundException(id);
        }

        contactRepository.deleteById(id);
        log.info("Contatto eliminato: id={}", id);

        eventPublisher.publishDeleted(id);
        log.debug("Evento DELETED pubblicato per id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> getAll(Pageable pageable) {
        log.debug("Recupero lista contatti: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ContactResponseDto> result = contactRepository.findAll(pageable)
                .map(ContactMapper::toResponseDto);

        log.debug("Contatti restituiti: {} su {} totali",
                result.getNumberOfElements(), result.getTotalElements());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponseDto> search(String term, Pageable pageable) {
        log.debug("Ricerca full-text: term='{}', page={}, size={}",
                term, pageable.getPageNumber(), pageable.getPageSize());

        Page<ContactResponseDto> result = contactRepository.searchByTerm(term, pageable)
                .map(ContactMapper::toResponseDto);

        log.debug("Risultati ricerca per '{}': {} trovati", term, result.getTotalElements());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto findByExactPhoneNumber(String phoneNumber) {
        log.debug("Ricerca per numero esatto: {}", phoneNumber);

        String normalized = normalizePhoneNumber(phoneNumber);
        Contact entity = contactRepository.findByPhoneNumber(normalized)
                .orElseThrow(() -> {
                    log.warn("Nessun contatto trovato per numero: {}", normalized);
                    return new ContactNotFoundException(normalized);
                });

        log.debug("Contatto trovato per numero {}: id={}", normalized, entity.getId());
        return ContactMapper.toResponseDto(entity);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[\\s-]", "");
    }

    private ContactEvent buildEvent(Contact contact, ContactEvent.EventType type) {
        return ContactEvent.builder()
                .eventType(type)
                .contactId(contact.getId())
                .surname(contact.getSurname())
                .name(contact.getName())
                .phoneNumber(contact.getPhoneNumber())
                .address(contact.getAddress())
                .latitude(contact.getGeodata() != null ? contact.getGeodata().getLatitude() : null)
                .longitude(contact.getGeodata() != null ? contact.getGeodata().getLongitude() : null)
                .otherInfo(contact.getOtherInfo())
                .occurredAt(LocalDateTime.now())
                .build();
    }
}