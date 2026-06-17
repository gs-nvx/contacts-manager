package com.example.contacts_search_processor.repository;

import com.example.contacts_search_processor.document.ContactDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContactSearchRepository extends ElasticsearchRepository<ContactDocument, String> {

    /**
     * Ricerca full-text su più campi con query multi_match di Elasticsearch.
     * Equivalente ES della query LIKE multipla che avevamo su MySQL,
     * ma molto più potente: scoring per rilevanza, stemming, fuzzy matching.
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["surname", "name", "address", "otherInfo"],
                "type": "best_fields",
                "fuzziness": "AUTO"
              }
            }
            """)
    Page<ContactDocument> findByFullText(String term, Pageable pageable);

    /**
     * Match esatto su phoneNumber (campo Keyword).
     * Spring Data ES genera automaticamente la query term dal nome del metodo.
     */
    Page<ContactDocument> findByPhoneNumber(String phoneNumber, Pageable pageable);
}