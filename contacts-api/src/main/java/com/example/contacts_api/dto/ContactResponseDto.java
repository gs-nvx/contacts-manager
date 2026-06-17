package com.example.contacts_api.dto;

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
public class ContactResponseDto {

    private Long id;
    private String surname;
    private String name;
    private String phoneNumber;
    private String address;
    private GeodataDto geodata;
    private String otherInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}