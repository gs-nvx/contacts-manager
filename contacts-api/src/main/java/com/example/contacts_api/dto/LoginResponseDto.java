package com.example.contacts_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String tokenType;
    private long expiresInSeconds;
}