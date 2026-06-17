package com.example.contacts_api.service.impl;

import com.example.contacts_api.dto.ContactRequestDto;
import com.example.contacts_api.dto.ContactResponseDto;
import com.example.contacts_api.dto.GeodataDto;
import com.example.contacts_api.entity.Contact;
import com.example.contacts_api.entity.Geodata;
import com.example.contacts_api.exception.ContactNotFoundException;
import com.example.contacts_api.exception.DuplicatePhoneNumberException;
import com.example.contacts_api.messaging.ContactEventPublisher;
import com.example.contacts_api.repository.ContactRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test per ContactServiceImpl.
 *
 * Usiamo @ExtendWith(MockitoExtension.class) invece di @SpringBootTest:
 * non carichiamo l'intero contesto Spring, solo la classe sotto test
 * con le sue dipendenze mocckate. I test sono molto più veloci e isolati.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactServiceImpl")
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactEventPublisher eventPublisher;

    @InjectMocks
    private ContactServiceImpl contactService;

    // ── Fixture comuni ────────────────────────────────────────────────────

    private Contact buildContact(Long id, String surname, String name, String phone) {
        return Contact.builder()
                .id(id)
                .surname(surname)
                .name(name)
                .phoneNumber(phone)
                .address("Via Roma 1")
                .geodata(new Geodata(45.0, 9.0))
                .otherInfo("note")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ContactRequestDto buildRequest(String surname, String name, String phone) {
        ContactRequestDto dto = new ContactRequestDto();
        dto.setSurname(surname);
        dto.setName(name);
        dto.setPhoneNumber(phone);
        dto.setAddress("Via Roma 1");
        dto.setGeodata(new GeodataDto(45.0, 9.0));
        dto.setOtherInfo("note");
        return dto;
    }

    // ═════════════════════════════════════════════════════════════════════
    // CREATE
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("crea contatto e pubblica evento CREATED")
        void create_success() {
            // ARRANGE
            ContactRequestDto request = buildRequest("Rossi", "Mario", "+39 333-1234567");
            Contact saved = buildContact(1L, "Rossi", "Mario", "+393331234567");

            when(contactRepository.existsByPhoneNumber("+393331234567")).thenReturn(false);
            when(contactRepository.save(any(Contact.class))).thenReturn(saved);

            // ACT
            ContactResponseDto result = contactService.create(request);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSurname()).isEqualTo("Rossi");
            assertThat(result.getPhoneNumber()).isEqualTo("+393331234567");

            // Verifica che il numero sia stato normalizzato prima del save
            ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);
            verify(contactRepository).save(contactCaptor.capture());
            assertThat(contactCaptor.getValue().getPhoneNumber()).isEqualTo("+393331234567");

            // Verifica che l'evento sia stato pubblicato
            verify(eventPublisher).publishCreated(any());
        }

        @Test
        @DisplayName("lancia DuplicatePhoneNumberException se il numero esiste già")
        void create_duplicatePhone_throwsException() {
            // ARRANGE
            ContactRequestDto request = buildRequest("Bianchi", "Luigi", "+39 333-1234567");
            when(contactRepository.existsByPhoneNumber("+393331234567")).thenReturn(true);

            // ACT & ASSERT
            assertThatThrownBy(() -> contactService.create(request))
                    .isInstanceOf(DuplicatePhoneNumberException.class)
                    .hasMessageContaining("+393331234567");

            // Verifica che save e publisher NON siano stati chiamati
            verify(contactRepository, never()).save(any());
            verify(eventPublisher, never()).publishCreated(any());
        }

        @Test
        @DisplayName("normalizza il numero prima del salvataggio")
        void create_normalizesPhoneNumber() {
            // ARRANGE — numero con spazi e trattini
            ContactRequestDto request = buildRequest("Verdi", "Anna", "+39 333 - 999 - 8888");
            Contact saved = buildContact(2L, "Verdi", "Anna", "+393339998888");

            when(contactRepository.existsByPhoneNumber("+393339998888")).thenReturn(false);
            when(contactRepository.save(any())).thenReturn(saved);

            // ACT
            contactService.create(request);

            // ASSERT — verifica che il numero passato al repository sia normalizzato
            verify(contactRepository).existsByPhoneNumber("+393339998888");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("aggiorna contatto esistente e pubblica evento UPDATED")
        void update_success() {
            // ARRANGE
            Contact existing = buildContact(1L, "Rossi", "Mario", "+393331234567");
            ContactRequestDto request = buildRequest("Rossi", "Mario Aggiornato", "+393331234567");
            Contact saved = buildContact(1L, "Rossi", "Mario Aggiornato", "+393331234567");

            when(contactRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(contactRepository.save(any())).thenReturn(saved);

            // ACT
            ContactResponseDto result = contactService.update(1L, request);

            // ASSERT
            assertThat(result.getName()).isEqualTo("Mario Aggiornato");
            verify(eventPublisher).publishUpdated(any());
        }

        @Test
        @DisplayName("lancia ContactNotFoundException se l'id non esiste")
        void update_notFound_throwsException() {
            // ARRANGE
            when(contactRepository.findById(99L)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> contactService.update(99L, buildRequest("X", "Y", "123")))
                    .isInstanceOf(ContactNotFoundException.class)
                    .hasMessageContaining("99");

            verify(contactRepository, never()).save(any());
            verify(eventPublisher, never()).publishUpdated(any());
        }

        @Test
        @DisplayName("lancia DuplicatePhoneNumberException se il nuovo numero è già usato da altro contatto")
        void update_newPhoneDuplicate_throwsException() {
            // ARRANGE
            Contact existing = buildContact(1L, "Rossi", "Mario", "+393331111111");
            ContactRequestDto request = buildRequest("Rossi", "Mario", "+39 333-2222222");

            when(contactRepository.findById(1L)).thenReturn(Optional.of(existing));
            // Il numero normalizzato "+393332222222" appartiene a un altro contatto
            when(contactRepository.existsByPhoneNumber("+393332222222")).thenReturn(true);

            // ACT & ASSERT
            assertThatThrownBy(() -> contactService.update(1L, request))
                    .isInstanceOf(DuplicatePhoneNumberException.class);

            verify(contactRepository, never()).save(any());
        }

        @Test
        @DisplayName("permette update con lo stesso numero del contatto corrente")
        void update_samePhone_success() {
            // ARRANGE — stesso numero, non deve verificare duplicati
            Contact existing = buildContact(1L, "Rossi", "Mario", "+393331234567");
            ContactRequestDto request = buildRequest("Rossi", "Mario Updated", "+39 333-1234567");
            Contact saved = buildContact(1L, "Rossi", "Mario Updated", "+393331234567");

            when(contactRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(contactRepository.save(any())).thenReturn(saved);

            // ACT
            ContactResponseDto result = contactService.update(1L, request);

            // ASSERT — existsByPhoneNumber NON deve essere chiamato per lo stesso numero
            verify(contactRepository, never()).existsByPhoneNumber(any());
            assertThat(result).isNotNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // GET BY ID
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("restituisce il contatto corretto")
        void getById_success() {
            Contact contact = buildContact(1L, "Rossi", "Mario", "+393331234567");
            when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

            ContactResponseDto result = contactService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSurname()).isEqualTo("Rossi");
            assertThat(result.getGeodata()).isNotNull();
            assertThat(result.getGeodata().getLatitude()).isEqualTo(45.0);
        }

        @Test
        @DisplayName("lancia ContactNotFoundException se non trovato")
        void getById_notFound_throwsException() {
            when(contactRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.getById(42L))
                    .isInstanceOf(ContactNotFoundException.class)
                    .hasMessageContaining("42");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // DELETE
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("elimina il contatto e pubblica evento DELETED")
        void delete_success() {
            when(contactRepository.existsById(1L)).thenReturn(true);

            contactService.delete(1L);

            verify(contactRepository).deleteById(1L);
            verify(eventPublisher).publishDeleted(1L);
        }

        @Test
        @DisplayName("lancia ContactNotFoundException se l'id non esiste")
        void delete_notFound_throwsException() {
            when(contactRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> contactService.delete(99L))
                    .isInstanceOf(ContactNotFoundException.class);

            verify(contactRepository, never()).deleteById(any());
            verify(eventPublisher, never()).publishDeleted(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // GET ALL
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("restituisce pagina di contatti")
        void getAll_returnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Contact> contacts = List.of(
                    buildContact(1L, "Rossi", "Mario", "+391111111111"),
                    buildContact(2L, "Bianchi", "Luigi", "+392222222222")
            );
            Page<Contact> page = new PageImpl<>(contacts, pageable, 2);
            when(contactRepository.findAll(pageable)).thenReturn(page);

            Page<ContactResponseDto> result = contactService.getAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Rossi");
        }

        @Test
        @DisplayName("restituisce pagina vuota se non ci sono contatti")
        void getAll_empty_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(contactRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<ContactResponseDto> result = contactService.getAll(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // SEARCH
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("restituisce i risultati della ricerca full-text")
        void search_returnsResults() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Contact> contacts = List.of(
                    buildContact(1L, "Rossi", "Mario", "+391111111111")
            );
            Page<Contact> page = new PageImpl<>(contacts, pageable, 1);
            when(contactRepository.searchByTerm("rossi", pageable)).thenReturn(page);

            Page<ContactResponseDto> result = contactService.search("rossi", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getSurname()).isEqualTo("Rossi");
        }

        @Test
        @DisplayName("restituisce pagina vuota se nessun risultato")
        void search_noResults_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(contactRepository.searchByTerm("xyz", pageable)).thenReturn(Page.empty(pageable));

            Page<ContactResponseDto> result = contactService.search("xyz", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // FIND BY EXACT PHONE NUMBER
    // ═════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("findByExactPhoneNumber()")
    class FindByExactPhone {

        @Test
        @DisplayName("trova il contatto con numero normalizzato")
        void findByPhone_success() {
            Contact contact = buildContact(1L, "Rossi", "Mario", "+393331234567");
            when(contactRepository.findByPhoneNumber("+393331234567"))
                    .thenReturn(Optional.of(contact));

            // Passa numero con formato "umano" — deve essere normalizzato internamente
            ContactResponseDto result = contactService.findByExactPhoneNumber("+39 333-1234567");

            assertThat(result.getPhoneNumber()).isEqualTo("+393331234567");
            verify(contactRepository).findByPhoneNumber("+393331234567");
        }

        @Test
        @DisplayName("lancia ContactNotFoundException se il numero non esiste")
        void findByPhone_notFound_throwsException() {
            when(contactRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.findByExactPhoneNumber("+39 000-0000000"))
                    .isInstanceOf(ContactNotFoundException.class)
                    .hasMessageContaining("+390000000000");
        }
    }
}