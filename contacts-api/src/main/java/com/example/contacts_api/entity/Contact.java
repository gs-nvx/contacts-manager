package com.example.contacts_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entità principale: rappresenta un contatto della rubrica.
 */
@Entity
@Table(name = "contacts", indexes = {
        @Index(name = "idx_phone_number", columnList = "phoneNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Numero di telefono: ricerca a match esatto richiesta dalla traccia.
     * Per questo è indicizzato (vedi @Index sopra) e marcato come unique
     * a livello applicativo per evitare duplicati.
     */
    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Embedded
    private Geodata geodata;

    @Column(name = "other_info", columnDefinition = "TEXT")
    private String otherInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}