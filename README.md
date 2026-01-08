# 🍸 Cocktail Project API

Applicazione Spring Boot che espone API REST per gestire cocktail, ingredienti, passaggi di preparazione e preferiti. La sicurezza è affidata a Keycloak (JWT), i dati vivono su MySQL e la documentazione è navigabile tramite Swagger. Con Docker Compose l'ambiente include tre contenitori: l'app Spring Boot (porta 8080), MySQL (3306) e Keycloak (8081).

## 🧰 Tecnologie
- Framework: Spring Boot 4.0.0 (parent in `pom.xml`)
- Linguaggio: Java 17
- Database: MySQL 8
- ORM: JPA/Hibernate
- Sicurezza: Spring Security + OAuth2 Resource Server (Keycloak JWT)
- API Docs: SpringDoc OpenAPI 2.3.0 (Swagger UI)
- Build: Maven (Maven Wrapper incluso)
- Container: Docker & Docker Compose

## 🚀 Avvio Rapido

### Prerequisiti
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### Avvio con Docker Compose
```bash
# Avvia tutti i servizi (MySQL, Keycloak, App)
docker compose down #se gia' precedentemente avviato
docker compose up -d --build

# Verifica lo stato
docker compose ps

# Visualizza i logs dell'app
docker compose logs -f cocktail-app
```

### Accesso ai Servizi
- API REST: http://localhost:8080/api/cocktails
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/v3/api-docs
- Keycloak Admin UI: http://localhost:8081 (admin/admin)

## 🏗️ Architettura

### Struttura a 3 livelli
```
┌─────────────────┐
│   CONTROLLER    │  Riceve HTTP
├─────────────────┤
│    SERVICE      │  Logica business
├─────────────────┤
│  REPOSITORY     │  Database access
├─────────────────┤
│    DATABASE     │  MySQL
└─────────────────┘
```

```
cocktail-project/
├── src/main/java/com/cocktail/cocktailproject/
│   ├── config/          # Configurazioni (Security, Swagger)
│   ├── controller/      # REST Controllers (Cocktail, Auth, Favoriti)
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA Entities
│   ├── repository/      # Spring Data Repositories
│   └── service/         # Business Logic
├── src/main/resources/  # application.properties, templates/static
├── docker-compose.yml   # Orchestrazione servizi
└── Dockerfile           # Build multi-stage
```

### Componenti
- Entity: `Cocktail`, `Ingrediente`, `Preparazione`, `UserFavorito`
- DTO: `CocktailDTO`, `CreateCocktailRequestDTO`, `IngredientiDTO`, `LoginRequestDTO`, `TokenResponseDTO`, `UserRegistrationDTO`
- Repository: `CocktailRepository`, `IngredienteRepository`, `PreparazioneRepository`, `UserFavoritoRepository`
- Service: `CocktailService`, `FavoritiService`, `KeycloakUserService`
- Controller: `CocktailController`, `AuthController`, `FavoritiController`
- Config: `SecurityConfig` (permessi OAuth2/JWT), `SpringDocConfig` (Swagger)

### Permessi e Sicurezza
- Pubblico: `/api/auth/**`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`), tutte le `GET` su `/api/cocktails/**`
- Richiede ruolo `SOLDIER`: `POST/PUT/DELETE` su `/api/cocktails/**`
- Richiede autenticazione: tutte le rotte sotto `/api/favoriti/**`
- I ruoli sono ottenuti da `realm_access.roles` nel JWT e mappati come `ROLE_<ruolo>`.

### Database Schema (logico)
```
cocktail (id, nome, descrizione, tempo_preparazione_minutes, note)
ingredienti (id, nome)
preparazione (id, cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
user_favoriti (id, user_id, cocktail_id)
```
Relazioni: Cocktail ↔ Preparazione (1:N); Ingrediente ↔ Preparazione (1:N); Cocktail ↔ Ingrediente (M:N via Preparazione); User ↔ Cocktail (M:N via user_favoriti).

### Flusso tipico (GET /api/cocktails)
Client → Controller → Service → Repository/JPA → MySQL → conversione in DTO → risposta JSON.

## 📚 API Endpoints (panoramica)

### Cocktails
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/cocktails` | Lista paginata cocktail (`page`, `size`) |
| GET | `/api/cocktails/{id}` | Dettaglio cocktail per ID |
| GET | `/api/cocktails/search?nome=xxx` | Cerca cocktail per nome (paginato) |
| GET | `/api/cocktails/ingredients` | Lista paginata ingredienti |
| POST | `/api/cocktails` | Crea nuovo cocktail (richiede `SOLDIER`) |
| PUT | `/api/cocktails/{id}` | Aggiorna cocktail (richiede `SOLDIER`) |
| DELETE | `/api/cocktails/{id}` | Elimina cocktail (richiede `SOLDIER`) |

### Favoriti (autenticazione richiesta)
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/favoriti` | Lista cocktail preferiti dell'utente |
| GET | `/api/favoriti/count` | Conteggio preferiti |
| GET | `/api/favoriti/check/{cocktailId}` | Verifica se un cocktail è nei preferiti |
| POST | `/api/favoriti/{cocktailId}` | Aggiungi ai preferiti |
| POST | `/api/favoriti/toggle/{cocktailId}` | Toggle aggiungi/rimuovi |
| DELETE | `/api/favoriti/{cocktailId}` | Rimuovi dai preferiti |

### Auth (pubblico)
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login ROPC, restituisce JWT |
| POST | `/api/auth/register` | Registrazione utente su Keycloak |

## 🧭 Tutorial: Login, CRUD e Favoriti

Di seguito una guida completa per usare il progetto tramite curl. Nota per Windows PowerShell: per impostare una variabile d'ambiente nella sessione usa `$env:NOME = "valore"`.

### 1) Login e ottenimento del token (Keycloak)
Richiesta:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "password1"
  }'
```
Risposta tipica:
```json
{
  "access_token": "eyJhbGciOi...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "...",
  "token_type": "Bearer",
  "scope": "profile email roles"
}
```
Salva il token (PowerShell):
```powershell
$env:TOKEN = "<incolla_access_token>"
```
Poi invia l'header Authorization: `Authorization: Bearer $env:TOKEN`.

Opzionale – Registrazione:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user1@example.com",
    "firstName": "User",
    "lastName": "One",
    "password": "password1",
    "confirmPassword": "password1"
  }'
```

### 2) Operazioni Cocktails (CRUD)

- Lista paginata (pubblico):
```bash
curl "http://localhost:8080/api/cocktails?page=0&size=10"
```

- Ricerca per nome (pubblico):
```bash
curl "http://localhost:8080/api/cocktails/search?nome=Margarita&page=0&size=5"
```

- Dettaglio per ID (pubblico):
```bash
curl "http://localhost:8080/api/cocktails/1"
```

- Crea cocktail (richiede ruolo SOLDIER):
```bash
curl -X POST "http://localhost:8080/api/cocktails" \
  -H "Authorization: Bearer $env:TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Margarita",
    "descrizione": "Cocktail classico messicano",
    "tempoPreparazioneMinutes": 5,
    "note": "Servire freddo",
    "ingredienti": [
      { "nome": "tequila", "quantita": 2, "unita": "oz" },
      { "nome": "lime juice", "quantita": 1, "unita": "oz" },
      { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
    ],
    "preparazione": "Shakerare con ghiaccio e servire"
  }'
```

- Aggiorna cocktail (richiede ruolo SOLDIER):
```bash
curl -X PUT "http://localhost:8080/api/cocktails/1" \
  -H "Authorization: Bearer $env:TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "nome": "Margarita (upd)",
    "descrizione": "Versione aggiornata",
    "tempoPreparazioneMinutes": 5,
    "note": null,
    "preparazione": []
  }'
```

- Elimina cocktail (richiede ruolo SOLDIER):
```bash
curl -X DELETE "http://localhost:8080/api/cocktails/1" \
  -H "Authorization: Bearer $env:TOKEN"
```

- Ingredienti disponibili (pubblico, paginato):
```bash
curl "http://localhost:8080/api/cocktails/ingredients?page=0&size=10"
```

### 3) Gestione Favoriti (autenticazione richiesta)

- Elenco preferiti dell'utente:
```bash
curl -H "Authorization: Bearer $env:TOKEN" "http://localhost:8080/api/favoriti"
```

- Aggiungi ai preferiti:
```bash
curl -X POST -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/1"
```

- Rimuovi dai preferiti:
```bash
curl -X DELETE -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/1"
```

- Toggle preferito (aggiunge se assente, rimuove se presente):
```bash
curl -X POST -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/toggle/1"
```

- Verifica se un cocktail è nei preferiti:
```bash
curl -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/check/1"
```

- Conteggio preferiti:
```bash
curl -H "Authorization: Bearer $env:TOKEN" \
  "http://localhost:8080/api/favoriti/count"
```

## 🔐 Dettagli Sicurezza

- Resource Server abilitato: i JWT vengono validati tramite `issuer-uri` e `jwk-set-uri` configurati in `application.properties`.
- Ruoli: richiesto `ROLE_SOLDIER` per operazioni di scrittura sui cocktail.
- Header richiesto: `Authorization: Bearer <access_token>`.

Estratto configurazione (`src/main/resources/application.properties`):
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://keycloak:8081/realms/cocktail-realm}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_ISSUER_URI:http://keycloak:8081/realms/cocktail-realm}/protocol/openid-connect/certs
```
Per sviluppo locale senza Docker, imposta `KEYCLOAK_ISSUER_URI` a `http://localhost:8081/realms/cocktail-realm` o modifica le proprietà di conseguenza.

## 🔧 Sviluppo Locale

### Build e Run Locale
```bash
# Build (salta i test)
./mvnw clean package -DskipTests

# Run (richiede MySQL su localhost:3306 con credenziali configurate)
./mvnw spring-boot:run

# Oppure esegui il JAR
java -jar target/cocktailproject-0.0.1-SNAPSHOT.jar
```

### Configurazione Database (locale)
Aggiorna `src/main/resources/application.properties` se non usi Docker:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/init
spring.datasource.username=cocktail_user
spring.datasource.password=cocktail_pass
```

## 🐳 Docker

### Servizi (docker-compose.yml)
- `cocktail-app`: Spring Boot su 8080 (container `cocktail-java-app`)
- `mysql`: MySQL 8.0 su 3306 (container `cocktail-db`)
- `keycloak`: Keycloak 24.0.5 su 8081 (container `keycloak`, realm import automatico)

### Comandi Utili
```bash
# Rebuild e restart
docker compose up -d --build

# Stop tutti i servizi
docker compose down

# Rimuovi anche i volumi
docker compose down -v

# Logs di un servizio specifico
docker compose logs -f mysql
```

## 📝 Note di Sviluppo
- JPA usa `spring.jpa.hibernate.ddl-auto=update` per aggiornare automaticamente lo schema.
- Dati di esempio caricati da `docker-entrypoint-initdb.d/init.sql`.
- Health check configurato su `/api/cocktails` nel compose.
- CORS attivo per tutti gli origins (`*`).
- Paginazione: tutti gli endpoint con lista accettano `page` e `size` (default `10`).

## 🤝 Contribuire
Progetto didattico per valutare competenze Java/Spring Boot. PR e issue sono benvenute.

## 📄 Licenza
Apache 2.0
