package com.example.contacts_api.mapper;

import com.example.contacts_api.dto.ContactRequestDto;
import com.example.contacts_api.dto.ContactResponseDto;
import com.example.contacts_api.dto.GeodataDto;
import com.example.contacts_api.entity.Contact;
import com.example.contacts_api.entity.Geodata;

/**
 * Mapper manuale Entity <-> DTO.
 * Classe di sole utility (final + costruttore privato): non ha stato,
 * non serve sia un bean Spring.
 */
public final class ContactMapper {

    private ContactMapper() {
    }

    public static Contact toEntity(ContactRequestDto dto) {
        return Contact.builder()
                .surname(dto.getSurname())
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .geodata(toGeodataEntity(dto.getGeodata()))
                .otherInfo(dto.getOtherInfo())
                .build();
    }

    /**
     * Aggiorna un'entità esistente con i dati della request,
     * preservando id e timestamp gestiti da JPA/Hibernate.
     */
    public static void updateEntity(Contact entity, ContactRequestDto dto) {
        entity.setSurname(dto.getSurname());
        entity.setName(dto.getName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setAddress(dto.getAddress());
        entity.setGeodata(toGeodataEntity(dto.getGeodata()));
        entity.setOtherInfo(dto.getOtherInfo());
    }

    public static ContactResponseDto toResponseDto(Contact entity) {
        return ContactResponseDto.builder()
                .id(entity.getId())
                .surname(entity.getSurname())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .address(entity.getAddress())
                .geodata(toGeodataDto(entity.getGeodata()))
                .otherInfo(entity.getOtherInfo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static Geodata toGeodataEntity(GeodataDto dto) {
        if (dto == null) {
            return null;
        }
        return new Geodata(dto.getLatitude(), dto.getLongitude());
    }

    private static GeodataDto toGeodataDto(Geodata entity) {
        if (entity == null) {
            return null;
        }
        return new GeodataDto(entity.getLatitude(), entity.getLongitude());
    }
}