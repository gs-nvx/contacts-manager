package com.example.contacts_api.controller;

import com.example.contacts_api.dto.ContactRequestDto;
import com.example.contacts_api.dto.ContactResponseDto;
import com.example.contacts_api.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Operazioni CRUD e di ricerca sulla rubrica contatti")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Crea un nuovo contatto")
    public ResponseEntity<ContactResponseDto> create(@Valid @RequestBody ContactRequestDto request) {
        ContactResponseDto created = contactService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna un contatto esistente")
    public ResponseEntity<ContactResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestDto request) {
        return ResponseEntity.ok(contactService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera un contatto per id")
    public ResponseEntity<ContactResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un contatto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Elenca tutti i contatti (paginato)")
    public ResponseEntity<Page<ContactResponseDto>> getAll(
            @PageableDefault(size = 20, sort = "surname") Pageable pageable) {
        return ResponseEntity.ok(contactService.getAll(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Ricerca full-text su cognome, nome, indirizzo, altre info")
    public ResponseEntity<Page<ContactResponseDto>> search(
            @Parameter(description = "Termine di ricerca") @RequestParam String term,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.search(term, pageable));
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Ricerca a match esatto per numero di telefono")
    public ResponseEntity<ContactResponseDto> getByPhoneNumber(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(contactService.findByExactPhoneNumber(phoneNumber));
    }
}