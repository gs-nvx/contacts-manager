package com.example.contacts_api.exception;

public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException(Long id) {
        super("Contatto non trovato con id: " + id);
    }

    // overload per chiarire quale campo ha dato esito negativo nella ricerca
    public ContactNotFoundException(String phoneNumber) {
        super("Nessun contatto trovato con numero di telefono: " + phoneNumber);
    }
}