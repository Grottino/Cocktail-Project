# Architettura Cocktail Project

## Struttura a 3 livelli

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

---

## Componenti

### Entity (Tabelle DB)
- **Cocktail** → nome, descrizione, tempo di preparazione
- **Ingrediente** → nome ingrediente (univoco)
- **Preparazione** → collega Cocktail + Ingrediente + step ricetta

### DTO (Dati per il client)
- **CocktailDTO** → cocktail formattato + ricetta dettagliata
- **StepPreparazioneDTO** → uno step della ricetta

### Repository (Accesso dati)
- **CocktailRepository** → CRUD cocktail + ricerca per nome
- **IngredienteRepository** → CRUD ingredienti
- **PreparazioneRepository** → CRUD step ricetta

### Service (Logica)
- **CocktailService** → CRUD + conversioni Entity ↔ DTO

### Controller (API REST)
- **CocktailController** → GET/POST/PUT/DELETE /api/cocktails

### Config
- **SecurityConfig** → Permessi OAuth2/JWT
- **OpenApiConfig** → Swagger documentation

---

## Endpoints

| Metodo | Endpoint | Permessi |
|--------|----------|----------|
| GET | `/api/cocktails` | Pubblico |
| GET | `/api/cocktails/{id}` | Pubblico |
| GET | `/api/cocktails/search?nome=xxx` | Pubblico |
| POST | `/api/cocktails` | Admin |
| PUT | `/api/cocktails/{id}` | Admin |
| DELETE | `/api/cocktails/{id}` | Admin |

---

## Flusso di una richiesta

**GET /api/cocktails:**

1. Client → HTTP request
2. Controller.getAllCocktails()
3. Service.getAllCocktails()
   - Chiama Repository.findAll()
   - Per ogni cocktail: carica gli step
   - Per ogni step: carica il nome ingrediente
   - Converte tutto in DTO
4. Database restituisce dati
5. Client riceve JSON

---

## Autenticazione

- **Provider:** Keycloak (OAuth2)
- **Token:** JWT
- **GET:** Pubblico
- **POST/PUT/DELETE:** Richiede ruolo `admin`

---

## Database Schema

```
cocktail (id, nome, descrizione, tempo_preparazione_minutes, note)
ingredienti (id, nome)
preparazione (id, cocktail_id, ingrediente_id, quantita, unita, step_order, istruzione)
```

**Relazioni:**
- Cocktail ↔ Preparazione (1:N)
- Ingrediente ↔ Preparazione (1:N)
- Cocktail ↔ Ingrediente (M:N via Preparazione)

---

## Come girare

**Con Docker:**
```bash
docker-compose up
cd Cocktail-Project
mvn spring-boot:run
```

**Swagger:** http://localhost:8080/swagger-ui.html

---

## Tecnologie

- Java 17
- Spring Boot 4.0.0
- MySQL 8
- JPA/Hibernate
- Spring Security + OAuth2
- Docker
- Maven

