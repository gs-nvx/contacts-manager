package com.example.contacts_api.repository;

import com.example.contacts_api.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Match esatto sul numero di telefono.
     * Derived query: Spring Data genera la query a partire dal nome del metodo.
     */
    Optional<Contact> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Ricerca full-text lato MySQL (fallback se Elasticsearch
     * non è disponibile, o per ambienti dove non vogliamo dipendere da ES).
     * Usa LIKE su più campi, case-insensitive grazie a LOWER().
     */
    @Query("""
            SELECT c FROM Contact c
            WHERE LOWER(c.surname) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(c.address) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(c.otherInfo) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<Contact> searchByTerm(@Param("term") String term, Pageable pageable);
}