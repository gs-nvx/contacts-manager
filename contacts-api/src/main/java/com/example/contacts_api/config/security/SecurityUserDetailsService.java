package com.example.contacts_api.config.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementazione in-memory di UserDetailsService.
 *
 * NOTA: in un sistema reale questa classe leggerebbe gli utenti da un repository
 * (es. UserRepository su tabella 'users'), con password hashate persistite.
 * La struttura (interfaccia UserDetailsService) è la stessa: questo è il punto
 * di estensione da sostituire, senza impatti sul resto della configurazione
 * Security (JwtAuthFilter, SecurityConfig restano invariati).
 */
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final Map<String, String> users;

    public SecurityUserDetailsService(PasswordEncoder passwordEncoder) {
        // Utente demo: username "admin", password "admin123"
        this.users = Map.of(
                "admin", passwordEncoder.encode("admin123")
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String encodedPassword = users.get(username);
        if (encodedPassword == null) {
            throw new UsernameNotFoundException("Utente non trovato: " + username);
        }

        return User.builder()
                .username(username)
                .password(encodedPassword)
                .roles("USER")
                .build();
    }
}