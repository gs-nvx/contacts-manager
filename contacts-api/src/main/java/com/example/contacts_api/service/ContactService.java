package com.example.contacts_api.service;

import com.example.contacts_api.dto.ContactRequestDto;
import com.example.contacts_api.dto.ContactResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {

    ContactResponseDto create(ContactRequestDto request);

    ContactResponseDto update(Long id, ContactRequestDto request);

    ContactResponseDto getById(Long id);

    void delete(Long id);

    Page<ContactResponseDto> getAll(Pageable pageable);

    Page<ContactResponseDto> search(String term, Pageable pageable);

    ContactResponseDto findByExactPhoneNumber(String phoneNumber);
}