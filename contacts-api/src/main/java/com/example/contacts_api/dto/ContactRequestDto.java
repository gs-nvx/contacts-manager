package com.example.contacts_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(max = 100)
    private String surname;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Il numero di telefono è obbligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s-]{6,20}$", message = "Formato numero di telefono non valido")
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    @Valid
    private GeodataDto geodata;

    private String otherInfo;
}