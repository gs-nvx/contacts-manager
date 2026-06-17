package com.example.contacts_api.exception;

public class DuplicatePhoneNumberException extends RuntimeException {

    public DuplicatePhoneNumberException(String phoneNumber) {
        super("Esiste già un contatto con il numero di telefono: " + phoneNumber);
    }
}