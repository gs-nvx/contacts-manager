# ── Stage 1: build ──────────────────────────────────────────
# Usa un'immagine Maven con Java 21 per compilare il progetto.
# "builder" è il nome di questo stage intermedio.
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia prima solo il pom.xml e scarichia le dipendenze.
# Questo sfrutta la cache dei layer Docker: se il pom non cambia,
# Maven non riscarica tutto ad ogni build — solo il codice cambiato.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia il sorgente e builda il jar, saltando i test
# (i test si eseguono in CI, non durante il build dell'immagine).
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: runtime ────────────────────────────────────────
# Immagine finale molto più leggera: solo JRE, niente Maven/sorgenti.
# eclipse-temurin è la distribuzione OpenJDK raccomandata da Adoptium.
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia solo il jar prodotto dallo stage di build.
COPY --from=builder /app/target/*.jar app.jar

# Porta esposta (documentativa, non pubblica automaticamente la porta).
EXPOSE 8080

# Avvio dell'applicazione.
ENTRYPOINT ["java", "-jar", "app.jar"]