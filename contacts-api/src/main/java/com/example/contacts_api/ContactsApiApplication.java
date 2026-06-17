package com.example.contacts_api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ContactsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactsApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner checkRabbitMQ(RabbitAdmin rabbitAdmin,
										   ConnectionFactory connectionFactory) {
		return args -> {
			// Forza apertura connessione e dichiarazione di exchange/code
			rabbitAdmin.initialize();
			log.info("RabbitMQ connesso. Exchange e code dichiarati.");
		};
	}

}
