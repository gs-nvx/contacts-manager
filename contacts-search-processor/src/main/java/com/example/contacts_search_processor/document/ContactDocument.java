package com.example.contacts_search_processor.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

/**
 * Documento Elasticsearch che rappresenta un contatto nell'indice.
 * È separato dall'entità JPA (Contact) di contacts-api:
 * questo documento è ottimizzato per la ricerca, non per la persistenza
 * relazionale. Può avere campi aggiuntivi, strutture diverse, etc.
 */
@Document(indexName = "contacts", createIndex = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDocument {

    @Id
    private String id; // String in ES, corrisponde al Long id di MySQL (convertito)

    @Field(type = FieldType.Text, analyzer = "standard")
    private String surname;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    /**
     * phoneNumber come Keyword: non analizzato, ricerca a match esatto.
     * FieldType.Keyword = nessuna tokenizzazione, il valore è indicizzato
     * così com'è — perfetto per ricerche esatte (es. codici, telefoni, email).
     */
    @Field(type = FieldType.Keyword)
    private String phoneNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String address;

    /**
     * GeoPoint: tipo nativo di Elasticsearch per coordinate geografiche.
     * Permette query geografiche avanzate (es. "contatti entro 10km da Roma")
     * non possibili con due semplici Double separati.
     */
    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String otherInfo;
}