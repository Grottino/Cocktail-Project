# Guida: Decodifica Token JWT Keycloak

## Panoramica

Questa guida spiega come utilizzare l'autenticazione JWT con Keycloak e come decodificare il token per visualizzare i ruoli dell'utente.

## Modifiche Implementate

### 1. SecurityConfig.java - Estrazione Ruoli da Keycloak

Il file `SecurityConfig.java` è stato aggiornato con un **JWT Authentication Converter** personalizzato che estrae correttamente i ruoli dal token JWT di Keycloak.

**Ruoli nel Token Keycloak:**
```json
{
  "realm_access": {
    "roles": [
      "SOLDIER",
      "offline_access",
      "uma_authorization",
      "default-roles-cocktail-realm"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  }
}
```

**Funzionamento:**
- I ruoli vengono estratti da `realm_access.roles`
- Ogni ruolo viene convertito in maiuscolo e prefissato con `ROLE_`
- Esempio: `SOLDIER` → `ROLE_SOLDIER`

### 2. JwtTokenService.java - Decodifica Token

Nuovo servizio per decodificare e analizzare i token JWT senza bisogno di validazione.

**Funzionalità:**
- `decodeToken(String token)`: Decodifica il token e restituisce tutte le informazioni
- `extractRealmRoles(String token)`: Estrae solo i ruoli del realm
- `isTokenExpired(String token)`: Verifica se il token è scaduto
- `extractUsername(String token)`: Estrae l'username dal token

### 3. AuthController.java - Nuovi Endpoint

#### **POST /api/auth/decode-token**
Endpoint pubblico per decodificare un token JWT.

**Request:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Token decodificato con successo",
  "token_info": {
    "subject": "12345-67890",
    "username": "mario.rossi",
    "email": "mario.rossi@example.com",
    "email_verified": true,
    "name": "Mario Rossi",
    "given_name": "Mario",
    "family_name": "Rossi",
    "realm_roles": [
      "SOLDIER",
      "offline_access",
      "uma_authorization"
    ],
    "resource_access": {
      "account": {
        "roles": ["manage-account", "view-profile"]
      }
    },
    "expires_at": 1702831200,
    "expires_at_readable": "Sun Dec 17 12:00:00 CET 2025",
    "issued_at": 1702827600,
    "issued_at_readable": "Sun Dec 17 11:00:00 CET 2025",
    "all_claims": { ... }
  }
}
```

#### **GET /api/auth/me**
Endpoint protetto che richiede autenticazione JWT.

**Header:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "success": true,
  "username": "12345-67890",
  "authorities": [
    "ROLE_SOLDIER",
    "ROLE_OFFLINE_ACCESS",
    "ROLE_UMA_AUTHORIZATION"
  ],
  "authenticated": true,
  "principal": { ... }
}
```

## Come Testare

### 1. Effettua il Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mario.rossi",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

### 2. Decodifica il Token (Metodo 1 - API Endpoint)

**Endpoint:** `POST /api/auth/decode-token`

```bash
curl -X POST http://localhost:8080/api/auth/decode-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

### 3. Verifica Informazioni Utente (Metodo 2 - Endpoint Protetto)

**Endpoint:** `GET /api/auth/me`

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Decodifica Manuale con jwt.io

Puoi anche decodificare il token manualmente:

1. Vai su https://jwt.io
2. Incolla il token nel campo "Encoded"
3. Visualizza il payload decodificato

## Configurazione Ruoli Keycloak

### Creare un Ruolo nel Realm

1. Accedi a Keycloak Admin Console: http://localhost:8081
2. Seleziona il realm `cocktail-realm`
3. Vai su **Realm Roles** → **Create Role**
4. Crea i ruoli necessari (es: `ADMIN`, `USER`, `SOLDIER`)

### Assegnare Ruoli agli Utenti

1. Vai su **Users** → Seleziona utente
2. Tab **Role Mapping**
3. Clicca **Assign role**
4. Seleziona i ruoli da assegnare (es: `ADMIN`, `SOLDIER`)

## Protezione Endpoint con Ruoli

Nel codice Spring Boot, puoi proteggere gli endpoint richiedendo ruoli specifici:

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/api/cocktails/{id}")
public ResponseEntity<?> deleteCocktail(@PathVariable Long id) {
    // Solo gli utenti con ruolo ADMIN possono accedere
}

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@PostMapping("/api/favoriti/{cocktailId}")
public ResponseEntity<?> addFavorite(@PathVariable Long cocktailId) {
    // Utenti con ruolo USER o ADMIN possono accedere
}
```

Oppure nella configurazione di sicurezza:

```java
.requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").hasRole("ADMIN")
.requestMatchers("/api/favoriti/**").hasAnyRole("USER", "ADMIN")
```

## Testare con Swagger UI

1. Apri Swagger UI: http://localhost:8080/swagger-ui.html
2. Effettua il login tramite `/api/auth/login`
3. Copia l'`access_token` dalla risposta
4. Clicca sul pulsante **Authorize** in alto a destra
5. Inserisci: `Bearer {access_token}`
6. Testa gli endpoint protetti (vedrai i ruoli estratti)

## Verifica Errori

### Token Scaduto
```json
{
  "error": "invalid_token",
  "error_description": "Token expired"
}
```

### Ruolo Insufficiente
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### Token Non Valido
```json
{
  "error": "invalid_token",
  "error_description": "Cannot convert access token to JSON"
}
```

## Struttura del Token JWT

Un token JWT è composto da tre parti separate da punti:

```
header.payload.signature
```

### Header
```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id"
}
```

### Payload (Claims)
```json
{
  "exp": 1702831200,
  "iat": 1702827600,
  "jti": "unique-id",
  "iss": "http://keycloak:8081/realms/cocktail-realm",
  "sub": "12345-67890",
  "typ": "Bearer",
  "azp": "cocktail-client",
  "session_state": "session-id",
  "scope": "openid profile email",
  "email_verified": true,
  "name": "Mario Rossi",
  "preferred_username": "mario.rossi",
  "given_name": "Mario",
  "family_name": "Rossi",
  "email": "mario.rossi@example.com",
  "realm_access": {
    "roles": ["SOLDIER", "offline_access", "uma_authorization"]
  },
  "resource_access": {
    "account": {
      "roles": ["manage-account", "view-profile"]
    }
  }
}
```

### Signature
Firma digitale per verificare l'integrità del token.

## Note Importanti

1. **Prefisso ROLE_**: Spring Security richiede che i ruoli abbiano il prefisso `ROLE_`. Il converter lo aggiunge automaticamente.

2. **Maiuscolo**: I ruoli vengono convertiti in maiuscolo per uniformità (es: `soldier` → `ROLE_SOLDIER`).

3. **Endpoint Pubblici vs Protetti**: 
   - `/api/auth/**` → Pubblici (login, register, decode-token)
   - `/api/cocktails` GET → Pubblico
   - `/api/cocktails` POST/PUT/DELETE → Solo ADMIN
   - `/api/favoriti/**` → Autenticazione richiesta

4. **Token Expiration**: Di default i token Keycloak hanno una durata di 5 minuti (300 secondi).

## Troubleshooting

### Problema: "No authorities found in JWT"
**Soluzione:** Verifica che il converter sia configurato correttamente in `SecurityConfig.java` e che i ruoli siano presenti in `realm_access.roles`.

### Problema: "403 Forbidden"
**Soluzione:** L'utente non ha il ruolo necessario. Verifica i ruoli assegnati in Keycloak.

### Problema: "401 Unauthorized"
**Soluzione:** Token non valido, scaduto o mancante. Effettua un nuovo login.

### Problema: "Token cannot be decoded"
**Soluzione:** Verifica che il token sia completo e nel formato corretto (3 parti separate da punti).

## Conclusione

Ora il sistema è configurato per:
✅ Estrarre correttamente i ruoli da Keycloak (`realm_access.roles`)
✅ Convertire i ruoli nel formato Spring Security (`ROLE_XXX`)
✅ Decodificare e visualizzare le informazioni del token
✅ Proteggere gli endpoint in base ai ruoli
✅ Testare l'autenticazione e i ruoli via API
