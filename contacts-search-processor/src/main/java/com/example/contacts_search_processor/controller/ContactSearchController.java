package com.example.contacts_search_processor.controller;

import com.example.contacts_search_processor.document.ContactDocument;
import com.example.contacts_search_processor.service.ContactSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Ricerca full-text e per numero di telefono su Elasticsearch")
public class ContactSearchController {

    private final ContactSearchService searchService;

    @GetMapping
    @Operation(summary = "Ricerca full-text su cognome, nome, indirizzo, altre info (con fuzzy matching)")
    public ResponseEntity<Page<ContactDocument>> search(
            @RequestParam String term,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(searchService.search(term, pageable));
    }

    @GetMapping("/phone")
    @Operation(summary = "Ricerca a match esatto per numero di telefono")
    public ResponseEntity<Page<ContactDocument>> findByPhone(
            @RequestParam String phoneNumber,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(searchService.findByPhone(phoneNumber, pageable));
    }
}