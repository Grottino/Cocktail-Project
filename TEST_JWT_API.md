# Test API - Decodifica Token JWT

Questo file contiene esempi pratici di chiamate API per testare l'autenticazione e la decodifica del token JWT.

## Prerequisiti

- Docker e Docker Compose installati
- Progetto avviato con `docker-compose up`
- Utente registrato su Keycloak

## 1. Registrazione Utente

Prima di tutto, crea un utente:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!",
    "confirmPassword": "Test123!",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Risposta Attesa:**
```json
{
  "success": true,
  "message": "Utente creato con successo",
  "username": "testuser",
  "email": "testuser@example.com"
}
```

## 2. Login e Ottenimento Token

Effettua il login per ottenere un token JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

**Risposta Attesa:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJxYzFfOW...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI2ZjQy...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scope": "openid profile email roles"
}
```

**💡 Salva il valore di `access_token` per i prossimi step!**

## 3. Decodifica Token (Metodo 1: Endpoint Dedicato)

Usa l'endpoint `/api/auth/decode-token` per vedere tutte le informazioni del token:

```bash
# Sostituisci YOUR_ACCESS_TOKEN con il token ottenuto dal login
curl -X POST http://localhost:8080/api/auth/decode-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_ACCESS_TOKEN"
  }'
```

**Risposta Attesa:**
```json
{
  "success": true,
  "message": "Token decodificato con successo",
  "token_info": {
    "subject": "12345678-90ab-cdef-1234-567890abcdef",
    "username": "testuser",
    "email": "testuser@example.com",
    "email_verified": false,
    "name": "Test User",
    "given_name": "Test",
    "family_name": "User",
    "realm_roles": [
      "USER",
      "offline_access",
      "uma_authorization",
      "default-roles-cocktail-realm"
    ],
    "resource_access": {
      "account": {
        "roles": [
          "manage-account",
          "manage-account-links",
          "view-profile"
        ]
      }
    },
    "expires_at": 1702831500,
    "expires_at_readable": "Sun Dec 17 12:05:00 CET 2025",
    "issued_at": 1702831200,
    "issued_at_readable": "Sun Dec 17 12:00:00 CET 2025",
    "all_claims": {
      "exp": 1702831500,
      "iat": 1702831200,
      "jti": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "iss": "http://localhost:8081/realms/cocktail-realm",
      "aud": "account",
      "sub": "12345678-90ab-cdef-1234-567890abcdef",
      "typ": "Bearer",
      "azp": "cocktail-client",
      "session_state": "session-id",
      "acr": "1",
      "realm_access": {
        "roles": ["USER", "offline_access", "uma_authorization"]
      },
      "scope": "openid profile email roles",
      "sid": "session-id",
      "email_verified": false,
      "name": "Test User",
      "preferred_username": "testuser",
      "given_name": "Test",
      "family_name": "User",
      "email": "testuser@example.com"
    }
  }
}
```

### 📌 Informazioni Importanti dal Token:

- **realm_roles**: Contiene i ruoli assegnati all'utente nel realm Keycloak
- **username**: L'username dell'utente (in `preferred_username`)
- **expires_at**: Timestamp di scadenza del token (Unix timestamp)
- **email**: Email dell'utente

## 4. Verifica Informazioni Utente (Metodo 2: Endpoint Protetto)

Usa l'endpoint `/api/auth/me` per vedere come Spring Security interpreta il token:

```bash
# Sostituisci YOUR_ACCESS_TOKEN con il token ottenuto dal login
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Risposta Attesa:**
```json
{
  "success": true,
  "username": "12345678-90ab-cdef-1234-567890abcdef",
  "authorities": [
    "ROLE_USER",
    "ROLE_OFFLINE_ACCESS",
    "ROLE_UMA_AUTHORIZATION",
    "ROLE_DEFAULT-ROLES-COCKTAIL-REALM"
  ],
  "authenticated": true,
  "principal": {
    "tokenValue": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIi...",
    "issuedAt": "2025-12-17T11:00:00Z",
    "expiresAt": "2025-12-17T11:05:00Z",
    "headers": {
      "alg": "RS256",
      "typ": "JWT",
      "kid": "key-id"
    },
    "claims": { ... }
  }
}
```

### 📌 Nota: Trasformazione Ruoli

Spring Security trasforma i ruoli da Keycloak:
- `USER` → `ROLE_USER`
- `offline_access` → `ROLE_OFFLINE_ACCESS`
- Tutti i ruoli vengono convertiti in maiuscolo e prefissati con `ROLE_`

## 5. Test Accesso con Ruolo ADMIN

### 5.1 Assegna Ruolo ADMIN in Keycloak

1. Apri Keycloak Admin Console: http://localhost:8081
2. Login con `admin` / `admin`
3. Seleziona realm `cocktail-realm`
4. Vai su **Realm Roles** → Crea ruolo `ADMIN` (se non esiste)
5. Vai su **Users** → Cerca `testuser` → **Role Mapping**
6. Clicca **Assign role** → Seleziona `ADMIN` → **Assign**

### 5.2 Effettua Nuovo Login

Dopo aver assegnato il ruolo, fai un nuovo login per ottenere un token aggiornato:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

### 5.3 Verifica Ruolo ADMIN

```bash
# Decodifica il nuovo token
curl -X POST http://localhost:8080/api/auth/decode-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "NEW_ACCESS_TOKEN"
  }'
```

Ora dovresti vedere `ADMIN` nella lista di `realm_roles`:
```json
{
  "realm_roles": [
    "ADMIN",
    "USER",
    "offline_access",
    "uma_authorization"
  ]
}
```

### 5.4 Test Endpoint Protetto (Solo ADMIN)

Prova a creare un cocktail (richiede ruolo ADMIN):

```bash
curl -X POST http://localhost:8080/api/cocktails \
  -H "Authorization: Bearer NEW_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Mojito",
    "descrizione": "Cocktail cubano a base di rum",
    "ingredienti": [
      {"nome": "Rum bianco", "quantita": "50ml"},
      {"nome": "Lime", "quantita": "1"},
      {"nome": "Zucchero", "quantita": "2 cucchiaini"},
      {"nome": "Menta", "quantita": "10 foglie"}
    ],
    "preparazione": [
      {"ordine": 1, "descrizione": "Pestare menta e lime con zucchero"},
      {"ordine": 2, "descrizione": "Aggiungere rum e ghiaccio"},
      {"ordine": 3, "descrizione": "Allungare con acqua frizzante"}
    ]
  }'
```

**Risposta con ruolo ADMIN:** ✅ 201 Created
**Risposta senza ruolo ADMIN:** ❌ 403 Forbidden

## 6. Test con Token Scaduto

Aspetta 5 minuti (il token scade dopo 300 secondi) e prova a usare il vecchio token:

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer EXPIRED_TOKEN"
```

**Risposta Attesa:**
```json
{
  "error": "invalid_token",
  "error_description": "Token expired"
}
```

## 7. Test con Token Non Valido

Prova con un token inventato:

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer abc123invalidtoken"
```

**Risposta Attesa:**
```json
{
  "error": "invalid_token",
  "error_description": "An error occurred while attempting to decode the Jwt"
}
```

## 8. Verifica Manuale su jwt.io

Puoi anche decodificare il token manualmente:

1. Vai su https://jwt.io
2. Incolla il token completo nel campo "Encoded"
3. Visualizza il payload nel campo "Decoded"

Esempio di payload:
```json
{
  "exp": 1702831500,
  "iat": 1702831200,
  "jti": "unique-id",
  "iss": "http://localhost:8081/realms/cocktail-realm",
  "sub": "user-id",
  "typ": "Bearer",
  "azp": "cocktail-client",
  "preferred_username": "testuser",
  "email": "testuser@example.com",
  "realm_access": {
    "roles": ["ADMIN", "USER"]
  }
}
```

## 9. PowerShell Script per Test Automatico

Salva questo script come `test-jwt.ps1`:

```powershell
# Test completo autenticazione JWT

Write-Host "=== TEST AUTENTICAZIONE JWT ===" -ForegroundColor Cyan

# 1. Login
Write-Host "`n1. Effettuo login..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"username":"testuser","password":"Test123!"}'

$token = $loginResponse.access_token
Write-Host "✓ Token ottenuto: $($token.Substring(0, 50))..." -ForegroundColor Green

# 2. Decodifica token
Write-Host "`n2. Decodifico il token..." -ForegroundColor Yellow
$decodeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/decode-token" `
  -Method Post `
  -ContentType "application/json" `
  -Body (@{token=$token} | ConvertTo-Json)

Write-Host "✓ Username: $($decodeResponse.token_info.username)" -ForegroundColor Green
Write-Host "✓ Email: $($decodeResponse.token_info.email)" -ForegroundColor Green
Write-Host "✓ Ruoli: $($decodeResponse.token_info.realm_roles -join ', ')" -ForegroundColor Green

# 3. Verifica informazioni utente
Write-Host "`n3. Verifico informazioni utente..." -ForegroundColor Yellow
$meResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/me" `
  -Method Get `
  -Headers @{Authorization="Bearer $token"}

Write-Host "✓ Autenticato: $($meResponse.authenticated)" -ForegroundColor Green
Write-Host "✓ Authorities: $($meResponse.authorities -join ', ')" -ForegroundColor Green

Write-Host "`n=== TEST COMPLETATO ===" -ForegroundColor Cyan
```

Esegui lo script:
```powershell
.\test-jwt.ps1
```

## Risoluzione Problemi

### Errore: "Connection refused"
- Verifica che il progetto sia avviato: `docker-compose ps`
- Controlla che Spring Boot sia in esecuzione sulla porta 8080

### Errore: "Unauthorized"
- Verifica che il token sia valido e non scaduto
- Controlla che l'header `Authorization` sia corretto: `Bearer {token}`

### Errore: "Forbidden"
- L'utente non ha il ruolo necessario
- Assegna il ruolo corretto in Keycloak

### Token non contiene ruoli
- Verifica che i ruoli siano assegnati all'utente in Keycloak
- Verifica che il campo `scope` includa "roles"
- Fai un nuovo login dopo aver assegnato i ruoli

## Note Finali

- I token hanno una durata di **5 minuti** (configurable in Keycloak)
- I ruoli vengono estratti da `realm_access.roles`
- Spring Security aggiunge automaticamente il prefisso `ROLE_` ai ruoli
- L'endpoint `/api/auth/decode-token` è pubblico e non richiede autenticazione
- L'endpoint `/api/auth/me` è protetto e richiede un token JWT valido
