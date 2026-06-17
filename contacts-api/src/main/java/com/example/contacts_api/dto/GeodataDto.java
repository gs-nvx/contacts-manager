package com.example.contacts_api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeodataDto {

    @DecimalMin(value = "-90.0", message = "La latitudine deve essere >= -90")
    @DecimalMax(value = "90.0", message = "La latitudine deve essere <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitudine deve essere >= -180")
    @DecimalMax(value = "180.0", message = "La longitudine deve essere <= 180")
    private Double longitude;
}