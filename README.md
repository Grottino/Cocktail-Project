# 🍸 Cocktail Project API

Applicazione Spring Boot per la gestione di cocktail, ingredienti e ricette. Include autenticazione OAuth2 (Keycloak), documentazione API Swagger, e deployment Docker.

## 🚀 Avvio Rapido

### Prerequisiti
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### Avvio con Docker Compose
```bash
# Avvia tutti i servizi (MySQL, Keycloak, App)
docker compose up -d

# Verifica lo stato
docker compose ps

# Visualizza i logs
docker compose logs -f cocktail-app
```

### Accesso ai Servizi
- **API REST**: http://localhost:8080/api/cocktails
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- **Keycloak Admin**: http://localhost:8081 (admin/admin)

## 📚 API Endpoints

### Cocktails
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/cocktails` | Lista tutti i cocktail |
| GET | `/api/cocktails/{id}` | Dettaglio cocktail per ID |
| GET | `/api/cocktails/search?nome=xxx` | Cerca cocktail per nome |
| POST | `/api/cocktails` | Crea nuovo cocktail |
| PUT | `/api/cocktails/{id}` | Aggiorna cocktail esistente |
| DELETE | `/api/cocktails/{id}` | Elimina cocktail |

### Esempio Risposta GET /api/cocktails
```json
[
  {
    "id": 1,
    "nome": "Last Word",
    "descrizione": "Cocktail del Proibizionismo a base di gin e Chartreuse.",
    "tempoPreparazioneMinutes": 2,
    "note": null,
    "preparazione": [
      {
        "stepOrder": 1,
        "ingrediente": "Gin",
        "quantita": "25.00",
        "unita": "ml",
        "istruzione": "Versare il gin nello shaker con ghiaccio."
      }
    ]
  }
]
```

## 🏗️ Architettura

```
cocktail-project/
├── src/main/java/com/cocktail/cocktailproject/
│   ├── config/          # Configurazioni (Security, Swagger)
│   ├── controller/      # REST Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── repository/     # Spring Data Repositories
│   └── service/        # Business Logic
├── docker-compose.yml  # Orchestrazione servizi
└── Dockerfile         # Build multi-stage
```

### Stack Tecnologico
- **Framework**: Spring Boot 4.0.0
- **Database**: MySQL 8.0
- **Security**: Spring Security + OAuth2 (Keycloak)
- **API Docs**: SpringDoc OpenAPI 2.3.0
- **ORM**: Hibernate + JPA
- **Build**: Maven
- **Container**: Docker

## 🔧 Sviluppo Locale

### Build e Run Locale
```bash
# Build
./mvnw clean package -DskipTests

# Run (richiede MySQL su localhost:3306)
./mvnw spring-boot:run

# Oppure
java -jar target/cocktailproject-0.0.1-SNAPSHOT.jar
```

### Configurazione Database
Modifica `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/init
spring.datasource.username=cocktail_user
spring.datasource.password=cocktail_pass
```

## 🔐 Sicurezza

### Configurazione Attuale
- Tutte le route sono **pubbliche** per testing
- OAuth2 Resource Server configurato ma **disabilitato temporaneamente**

### Abilitare Keycloak Authentication
Decommenta in `SecurityConfig.java`:
```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
```

## 🧪 Test API con curl

```bash
# Lista cocktails
curl http://localhost:8080/api/cocktails

# Cerca per nome
curl "http://localhost:8080/api/cocktails/search?nome=Last"

# Crea nuovo cocktail
curl -X POST http://localhost:8080/api/cocktails \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Mojito",
    "descrizione": "Cocktail cubano rinfrescante",
    "tempoPreparazioneMinutes": 5
  }'
```

## 📊 Database Schema

### Tabelle
- **cocktail**: Informazioni base cocktail
- **ingredienti**: Lista ingredienti disponibili
- **preparazione**: Step ricetta (join table)

### Relazioni
```
cocktail 1---N preparazione N---1 ingredienti
```

## 🐳 Docker

### Servizi
- **cocktail-app**: Applicazione Spring Boot (porta 8080)
- **mysql**: Database MySQL 8.0 (porta 3306)
- **keycloak**: Identity Provider (porta 8081)

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

- JPA usa `ddl-auto=update` per aggiornare automaticamente lo schema
- Dati di esempio caricati da `init.sql`
- Health check su `/api/cocktails` endpoint
- CORS abilitato per tutti gli origins (`*`)

## 🤝 Contribuire

Questo è un progetto didattico per valutare competenze Java/Spring Boot.

## 📄 Licenza

Apache 2.0
