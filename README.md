# Contacts Manager

Applicazione web per la gestione di una rubrica telefonica, sviluppata come progetto tecnico per un colloquio di lavoro Java Developer.

---

## Indice

- [Architettura](#architettura)
- [Stack tecnologico](#stack-tecnologico)
- [Struttura del progetto](#struttura-del-progetto)
- [Requisiti](#requisiti)
- [Avvio rapido](#avvio-rapido)
- [Configurazione](#configurazione)
- [API Reference](#api-reference)
- [Funzionalità](#funzionalità)
- [Scelte implementative](#scelte-implementative)
- [Estensioni future](#estensioni-future)

---

## Architettura

Il sistema è composto da due microservizi Spring Boot che comunicano in modo asincrono tramite RabbitMQ, seguendo il pattern **Event-Driven Architecture**.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Browser / Client                        │
│                          index.html                             │
└──────────────────┬────────────────────────┬────────────────────┘
                   │ REST (8080)             │ REST (8081)
                   ▼                         ▼
┌──────────────────────────┐   ┌─────────────────────────────────┐
│      contacts-api        │   │   contacts-search-processor     │
│                          │   │                                 │
│  Spring Boot 4.x         │   │  Spring Boot 4.x                │
│  Spring Security + JWT   │   │  Spring Data Elasticsearch      │
│  Spring Data JPA         │   │  Spring AMQP (consumer)         │
│  Spring AMQP (producer)  │   │                                 │
│  Liquibase               │   └────────────────┬────────────────┘
└────────┬─────────────────┘                    │
         │                    ┌─────────────────▼────────────────┐
         │ pubblica eventi    │           RabbitMQ               │
         └───────────────────►│                                  │
                              │  Exchange: contacts.exchange     │
         │ CRUD               │  Queue: created / updated /      │
         ▼                    │         deleted                  │
┌─────────────────┐           └──────────────────────────────────┘
│      MySQL      │
│  contacts_db    │           ┌──────────────────────────────────┐
│                 │           │         Elasticsearch            │
└─────────────────┘           │    indice: contacts              │
                              │    full-text + geo search        │
                              └──────────────────────────────────┘
```

### Flusso dati

1. Il client chiama `contacts-api` per operazioni CRUD (autenticazione JWT richiesta).
2. `contacts-api` persiste il dato su **MySQL** (fonte di verità).
3. `contacts-api` pubblica un evento (`CREATED` / `UPDATED` / `DELETED`) su **RabbitMQ**.
4. `contacts-search-processor` consuma l'evento e aggiorna l'indice su **Elasticsearch**.
5. Le ricerche full-text e per telefono vengono eseguite direttamente su `contacts-search-processor` (porta 8081), che interroga Elasticsearch.

Il disaccoppiamento garantisce che un eventuale down del processor non impatti le operazioni CRUD principali.

---

## Stack tecnologico

| Layer                | Tecnologia                                |
| -------------------- | ----------------------------------------- |
| Backend API          | Java 21, Spring Boot 4.x, Spring Data JPA |
| Autenticazione       | Spring Security, JWT (JJWT 0.12.x)        |
| Database relazionale | MySQL 8.x                                 |
| Migration schema     | Liquibase                                 |
| Message broker       | RabbitMQ 3.x                              |
| Search engine        | Elasticsearch 8.15                        |
| Backend Search       | Spring Data Elasticsearch                 |
| Documentazione API   | SpringDoc OpenAPI (Swagger UI)            |
| Monitoring           | Spring Boot Actuator                      |
| Frontend             | HTML5, Bootstrap 5, JavaScript vanilla    |
| Containerizzazione   | Docker, Docker Compose                    |

---

## Struttura del progetto

```
contacts-project/
├── docker-compose.yml
├── README.md
├── frontend/
│   └── index.html
├── contacts-api/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/contacts_api/
│       │   ├── config/
│       │   │   ├── OpenApiConfig.java
│       │   │   ├── RabbitMQConfig.java
│       │   │   └── security/
│       │   │       ├── JwtAuthFilter.java
│       │   │       ├── JwtService.java
│       │   │       ├── PasswordEncoderConfig.java
│       │   │       ├── SecurityConfig.java
│       │   │       └── SecurityUserDetailsService.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   └── ContactController.java
│       │   ├── dto/
│       │   │   ├── ContactRequestDto.java
│       │   │   ├── ContactResponseDto.java
│       │   │   ├── GeodataDto.java
│       │   │   ├── LoginRequestDto.java
│       │   │   └── LoginResponseDto.java
│       │   ├── entity/
│       │   │   ├── Contact.java
│       │   │   └── Geodata.java
│       │   ├── exception/
│       │   │   ├── ContactNotFoundException.java
│       │   │   ├── DuplicatePhoneNumberException.java
│       │   │   ├── ErrorResponse.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── mapper/
│       │   │   └── ContactMapper.java
│       │   ├── messaging/
│       │   │   ├── ContactEventPublisher.java
│       │   │   └── event/
│       │   │       └── ContactEvent.java
│       │   ├── repository/
│       │   │   └── ContactRepository.java
│       │   └── service/
│       │       ├── ContactService.java
│       │       └── impl/
│       │           └── ContactServiceImpl.java
│       └── resources/
│           ├── application.yml
│           └── db/changelog/
│               ├── db.changelog-master.yaml
│               └── changes/
│                   └── 001-create-contacts-table.yaml
└── contacts-search-processor/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/example/contacts_search_processor/
        │   ├── config/
        │   │   ├── CorsConfig.java
        │   │   └── RabbitMQConfig.java
        │   ├── controller/
        │   │   └── ContactSearchController.java
        │   ├── document/
        │   │   └── ContactDocument.java
        │   ├── messaging/
        │   │   ├── ContactEventConsumer.java
        │   │   └── event/
        │   │       └── ContactEvent.java
        │   ├── repository/
        │   │   └── ContactSearchRepository.java
        │   └── service/
        │       └── ContactSearchService.java
        └── resources/
            └── application.yml
```

---

## Requisiti

- **Docker** e **Docker Compose** (versione Compose V2, `docker compose` senza trattino)
- Nessun altro requisito: Java, Maven, MySQL, RabbitMQ ed Elasticsearch sono tutti containerizzati

---

## Avvio rapido

### 1. Clona il repository

//TODO: caricare codice su git

```bash
git clone <url-repository>
cd contacts-project
```

### 2. Avvia lo stack completo

```bash
docker compose up --build
```

La prima esecuzione richiede 5-10 minuti (download immagini Docker + compilazione Maven).

### 3. Verifica che tutti i servizi siano attivi

```bash
docker compose ps
```

Tutti i servizi devono essere in stato `running` (healthy):

| Servizio                  | URL                                          |
| ------------------------- | -------------------------------------------- |
| contacts-api              | http://localhost:8080                        |
| contacts-search-processor | http://localhost:8081                        |
| Swagger UI (API)          | http://localhost:8080/swagger-ui/index.html  |
| Swagger UI (Search)       | http://localhost:8081/swagger-ui/index.html  |
| RabbitMQ Management       | http://localhost:15672 (admin / admin123)    |
| Elasticsearch             | http://localhost:9200 (elastic / elastic123) |

### 4. Apri il frontend

Apri `frontend/index.html` nel browser.

Credenziali di default: `admin` / `admin123`

### 5. Ferma lo stack

```bash
# Ferma i container (i dati sono persistiti nei volumi)
docker compose down

# Reset completo inclusi i volumi (tutti i dati verranno cancellati)
docker compose down -v
```

---

## Configurazione

### Variabili d'ambiente principali

//TODO: impostare sugli application.yaml le variabili di ambiente

Le variabili d'ambiente nel `docker-compose.yml` sovrascrivono i valori in `application.yml` (Spring Boot Externalized Configuration — 12-Factor App).

| Variabile                    | Default                               | Descrizione                                       |
| ---------------------------- | ------------------------------------- | ------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | `jdbc:mysql://mysql:3306/contacts_db` | URL connessione MySQL                             |
| `SPRING_DATASOURCE_USERNAME` | `contacts_user`                       | Utente MySQL                                      |
| `SPRING_DATASOURCE_PASSWORD` | `ContactsPass123!`                    | Password MySQL                                    |
| `SPRING_RABBITMQ_HOST`       | `rabbitmq`                            | Host RabbitMQ                                     |
| `SPRING_ELASTICSEARCH_URIS`  | `http://elasticsearch:9200`           | URI Elasticsearch                                 |
| `APP_JWT_SECRET`             | _(stringa base64)_                    | Secret per firma JWT — **cambiare in produzione** |
| `APP_JWT_EXPIRATION_MS`      | `3600000`                             | Scadenza token JWT in ms (default: 1 ora)         |

### Utenti

L'autenticazione è in-memory con un utente di default:

| Username | Password | Ruolo |
| -------- | -------- | ----- |
| admin    | admin123 | USER  |

> In un sistema in produzione gli utenti sarebbero persistiti su database con password hashate tramite BCrypt.

---

## API Reference

### contacts-api (porta 8080)

Tutti gli endpoint `/api/v1/contacts/**` richiedono autenticazione Bearer JWT.

#### Autenticazione

```
POST /api/v1/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Risposta:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

#### Contatti

| Metodo   | Endpoint                               | Descrizione                                     |
| -------- | -------------------------------------- | ----------------------------------------------- |
| `GET`    | `/api/v1/contacts`                     | Lista paginata (params: `page`, `size`, `sort`) |
| `POST`   | `/api/v1/contacts`                     | Crea nuovo contatto                             |
| `GET`    | `/api/v1/contacts/{id}`                | Recupera contatto per id                        |
| `PUT`    | `/api/v1/contacts/{id}`                | Aggiorna contatto                               |
| `DELETE` | `/api/v1/contacts/{id}`                | Elimina contatto                                |
| `GET`    | `/api/v1/contacts/search?term=`        | Ricerca full-text su MySQL (LIKE)               |
| `GET`    | `/api/v1/contacts/phone/{phoneNumber}` | Ricerca a match esatto per telefono             |

Esempio body contatto:

```json
{
  "surname": "Rossi",
  "name": "Mario",
  "phoneNumber": "+39 333 1234567",
  "address": "Via Roma 1, Milano",
  "geodata": {
    "latitude": 45.4642,
    "longitude": 9.19
  },
  "otherInfo": "Cliente storico"
}
```

### contacts-search-processor (porta 8081)

| Metodo | Endpoint                            | Descrizione                                |
| ------ | ----------------------------------- | ------------------------------------------ |
| `GET`  | `/api/v1/search?term=`              | Ricerca full-text su Elasticsearch (fuzzy) |
| `GET`  | `/api/v1/search/phone?phoneNumber=` | Match esatto su numero di telefono         |

---

## Funzionalità

### CRUD Contatti

Creazione, lettura, aggiornamento ed eliminazione contatti con i campi: cognome, nome, numero di telefono, indirizzo, coordinate geografiche (lat/lon), note aggiuntive.

### Ricerca

- **Full-text**: ricerca su cognome, nome, indirizzo e note tramite Elasticsearch con supporto fuzzy matching (tolleranza ai typo) e scoring per rilevanza.
- **Match esatto**: ricerca per numero di telefono normalizzato (spazi e trattini rimossi automaticamente).
- **Fallback MySQL**: endpoint di ricerca LIKE direttamente su MySQL disponibile su `contacts-api`.

### Sicurezza

- Autenticazione stateless tramite JWT (Bearer token).
- Password hashate con BCrypt.
- Endpoint pubblici limitati a `/auth/login`, Swagger UI e `/actuator/health`.
- CORS configurato esplicitamente.

### Qualità e osservabilità

- **Liquibase**: migration dello schema versionata e ripetibile.
- **Swagger UI**: documentazione API interattiva autogenerata su entrambi i servizi.
- **Spring Boot Actuator**: endpoint `/actuator/health` e `/actuator/metrics` per monitoring.
- **Paginazione**: tutti gli endpoint di lista supportano paginazione e ordinamento.
- **Gestione errori centralizzata**: `GlobalExceptionHandler` con risposte di errore uniformi (timestamp, status, message, validationErrors).

### Messaggistica asincrona

Ad ogni operazione CRUD, `contacts-api` pubblica un evento su RabbitMQ (`CREATED` / `UPDATED` / `DELETED`). `contacts-search-processor` consuma gli eventi e mantiene l'indice Elasticsearch sincronizzato. Il fallimento del processor non impatta le operazioni CRUD principali (resilienza by design).

---

## Scelte implementative

### Perché due microservizi separati

Il CRUD e la ricerca hanno requisiti diversi: MySQL è ottimale per la consistenza dei dati relazionali, Elasticsearch per la ricerca full-text. Separarli permette di scalare i due servizi indipendentemente (es. più istanze del processor in caso di alto volume di eventi) e di sostituire un componente senza impattare l'altro.

### Perché RabbitMQ invece di chiamate REST dirette

La comunicazione asincrona via eventi disaccoppia completamente i due servizi. Se il processor è down, gli eventi rimangono in coda e vengono processati alla riconnessione — nessuna perdita di dati. Con REST sincrono, un down del processor causerebbe errori sulle chiamate CRUD.

### Perché Liquibase invece di `ddl-auto: update`

`ddl-auto: update` è comodo in sviluppo ma pericoloso in produzione: non rimuove colonne, può prendere decisioni inattese, non è idempotente. Liquibase tiene traccia delle migration applicate (tabella `DATABASECHANGELOG`), è ripetibile su ambienti diversi e permette rollback controllati.

### Perché JWT stateless

Nessuna sessione lato server da replicare tra istanze — l'architettura è scalabile orizzontalmente senza infrastruttura aggiuntiva (es. Redis per le sessioni). Ogni istanza di `contacts-api` può validare autonomamente il token.

### Normalizzazione del numero di telefono

I numeri vengono normalizzati (rimozione spazi e trattini) prima del salvataggio e della ricerca, garantendo che `+39 333-123` e `+39333123` producano lo stesso match esatto.

---

## Estensioni future

- **Autenticazione su database**: sostituire `SecurityUserDetailsService` in-memory con un `UserRepository` JPA — il contratto (`UserDetailsService`) è già rispettato, la modifica è localizzata.
- **Migration Flyway/Liquibase in produzione**: aggiungere changeset per ogni modifica allo schema, con strategia di rollback.
- **Spring Cloud Config**: centralizzare la configurazione dei due microservizi in un config server, eliminando la duplicazione delle property RabbitMQ.
- **Retry e Dead Letter Queue**: aggiungere strategia di retry su RabbitMQ per messaggi non processabili, con DLQ per l'analisi degli errori.
- **Cluster Elasticsearch**: passare da `single-node` a configurazione multi-nodo per alta disponibilità e sharding.
- **Cache Redis**: aggiungere `@Cacheable` sulle letture frequenti in `contacts-api` per ridurre il carico su MySQL.
- **Test**: aggiungere unit test (JUnit 5 + Mockito) (fatto solo su ContactServiceImpl) e integration test (Testcontainers per MySQL, RabbitMQ ed Elasticsearch in isolamento).
- **CI/CD**: pipeline GitHub Actions per build, test e push delle immagini Docker su registry.
- **Exception Handling**: rafforzare la gestione delle eccezioni (completare eccezioni custom e inserire il metodo di handling nel GlobalExceptionHandler)
- **Logging**: inserire tutti i log necessari (fatto solo su ContactServiceImpl)
