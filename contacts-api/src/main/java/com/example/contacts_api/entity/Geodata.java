package com.example.contacts_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object che rappresenta una coppia di coordinate geografiche.
 * Annotato come @Embeddable: non ha vita propria, viene "incorporato"
 * nelle colonne della tabella che lo contiene (Contact).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Geodata {

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}