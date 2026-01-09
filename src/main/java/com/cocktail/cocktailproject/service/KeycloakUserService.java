package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.LoginRequestDTO;
import com.cocktail.cocktailproject.dto.TokenResponseDTO;
import com.cocktail.cocktailproject.dto.UserRegistrationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * KeycloakUserService - Integrazione con Keycloak per autenticazione e gestione utenti
 * 
 * Operazioni supportate:
 * 1. Login ROPC (Resource Owner Password Credentials)
 *    - L'utente invia username/password
 *    - Keycloak restituisce access_token JWT
 *    - Token usato per autenticare le chiamate API protette
 * 
 * 2. Registrazione utenti
 *    - Crea nuovo utente su Keycloak
 *    - Imposta password
 *    - Assegna ruolo di default (USER)
 *    - Verifica unicità username ed email
 * 
 * 3. Gestione ruoli
 *    - Assegna ruoli realm agli utenti
 *    - I ruoli determinano i permessi (es: SOLDIER per operazioni write)
 * 
 * Configurazione:
 * - Le credenziali admin Keycloak sono in application.properties
 * - Il realm di destinazione è configurabile
 * - Supporta client con o senza client_secret
 */
@Service
public class KeycloakUserService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:swagger-ui}")
    private String loginClientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserService.class);
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    // Constructor Injection per ObjectMapper e RestTemplate
    public KeycloakUserService(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Effettua login con Resource Owner Password Credentials (ROPC)
     * 
     * @param loginRequest Username e password dell'utente
     * @return Token response con access_token
     * @throws RuntimeException Se le credenziali sono errate o il login fallisce
     */
    public TokenResponseDTO login(LoginRequestDTO loginRequest) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", loginClientId);
        body.add("username", loginRequest.getUsername());
        body.add("password", loginRequest.getPassword());
        body.add("scope", "openid profile email roles");

        if (clientSecret != null && !clientSecret.isEmpty()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            MediaType contentType = response.getHeaders().getContentType();
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("Risposta vuota dal server di autenticazione.");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Keycloak /token status={} contentType={} bodyPreview={}...",
                        response.getStatusCode(),
                        contentType,
                        responseBody.substring(0, Math.min(responseBody.length(), 200))
                );
            }

            if (contentType == null || MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                try {
                    return objectMapper.readValue(responseBody, TokenResponseDTO.class);
                } catch (JsonProcessingException ex) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error_description").asText());
                        }
                        if (node.has("error")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error").asText());
                        }
                    } catch (Exception ignore) {}
                    throw new RuntimeException("Risposta non valida dal server di autenticazione (JSON non parsabile).");
                }
            }

            throw new RuntimeException("Risposta inattesa dal server di autenticazione: " +
                    (contentType != null ? contentType.toString() : "contenuto sconosciuto") +
                    ". Dettagli: " + responseBody.substring(0, Math.min(responseBody.length(), 300))
            );

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            MediaType ct = e.getResponseHeaders() != null ? e.getResponseHeaders().getContentType() : null;

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                String msg = "Credenziali non valide. Username o password errati.";
                if (responseBody != null && !responseBody.isBlank()) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            msg = node.get("error_description").asText();
                        }
                    } catch (Exception ignore) {}
                }
                throw new RuntimeException(msg);
            }

            if (responseBody != null && !responseBody.isBlank()) {
                if (ct != null && MediaType.APPLICATION_JSON.isCompatibleWith(ct)) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error_description").asText(), e);
                        }
                        if (node.has("error")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error").asText(), e);
                        }
                    } catch (Exception ignore) {}
                }
                throw new RuntimeException("Errore durante il login: " + responseBody.substring(0, Math.min(responseBody.length(), 300)), e);
            }

            throw new RuntimeException("Errore durante il login: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il login: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una connessione admin a Keycloak
     */
    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    /**
     * Verifica se un utente esiste già (per username o email)
     * 
     * @param username Username da verificare
     * @param email Email da verificare
     * @return true se l'utente esiste, false altrimenti
     */
    public boolean userExists(String username, String email) {
        try (Keycloak keycloak = getKeycloakInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Cerca per username
            List<UserRepresentation> usersByUsername = usersResource.search(username, true);
            if (!usersByUsername.isEmpty()) {
                return true;
            }

            // Cerca per email
            List<UserRepresentation> usersByEmail = usersResource.search(null, null, null, email, 0, 1);
            return !usersByEmail.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la verifica dell'utente: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nuovo utente su Keycloak
     * 
     * @param registrationDTO Dati dell'utente da creare
     * @return Messaggio di successo
     * @throws RuntimeException Se l'utente esiste già o se la creazione fallisce
     */
    public String createUser(UserRegistrationDTO registrationDTO) {
        // Verifica se l'utente esiste già
        if (userExists(registrationDTO.getUsername(), registrationDTO.getEmail())) {
            throw new RuntimeException("Username o email già in uso");
        }

        try (Keycloak keycloak = getKeycloakInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Crea la rappresentazione dell'utente
            UserRepresentation user = new UserRepresentation();
            user.setUsername(registrationDTO.getUsername());
            user.setEmail(registrationDTO.getEmail());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false); // L'utente dovrà verificare l'email (se configurato)

            // Crea l'utente
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                throw new RuntimeException("Errore durante la creazione dell'utente: " + errorMessage);
            }

            // Ottieni l'ID dell'utente appena creato
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Imposta la password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(registrationDTO.getPassword());
            credential.setTemporary(false); // Non richiede cambio password al primo accesso

            usersResource.get(userId).resetPassword(credential);

            // Assegna il ruolo "User" di default
            assignRoleToUser(keycloak, realmResource, userId, "USER");

            response.close();
            return "Utente creato con successo";

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la creazione dell'utente: " + e.getMessage(), e);
        }
    }

    /**
     * Assegna un ruolo realm a un utente
     * 
     * @param keycloak Istanza Keycloak
     * @param realmResource Risorsa realm
     * @param userId ID dell'utente
     * @param roleName Nome del ruolo da assegnare
     */
    private void assignRoleToUser(Keycloak keycloak, RealmResource realmResource, String userId, String roleName) {
        try {
            // Ottieni il ruolo realm
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Assegna il ruolo all'utente
            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));

        } catch (Exception e) {
            // Log dell'errore ma non blocca la creazione utente
            System.err.println("Attenzione: impossibile assegnare il ruolo '" + roleName + "' all'utente: " + e.getMessage());
        }
    }
}
