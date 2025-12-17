package com.cocktail.cocktailproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JwtTokenService - Servizio per decodificare e analizzare i token JWT
 * 
 * Questo service permette di:
 * - Decodificare il token JWT senza validazione
 * - Estrarre le informazioni dell'utente (subject, email, username)
 * - Estrarre i ruoli da realm_access e resource_access
 * - Visualizzare tutte le claims del token
 */
@Service
public class JwtTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);
    private final ObjectMapper objectMapper;

    public JwtTokenService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Decodifica un token JWT e ne estrae tutte le informazioni
     * 
     * @param token Token JWT (può essere con o senza il prefisso "Bearer ")
     * @return Map contenente tutte le informazioni del token
     * @throws RuntimeException Se il token non è valido o non può essere decodificato
     */
    public Map<String, Object> decodeToken(String token) {
        try {
            // Rimuovi il prefisso "Bearer " se presente
            String cleanToken = token.replace("Bearer ", "").trim();

            // Un JWT è composto da tre parti separate da punti: header.payload.signature
            String[] parts = cleanToken.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Token JWT non valido: deve contenere 3 parti (header.payload.signature)");
            }

            // Decodifica il payload (seconda parte)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse del JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // Crea la risposta con le informazioni principali
            Map<String, Object> result = new HashMap<>();
            result.put("subject", claims.get("sub"));
            result.put("username", claims.get("preferred_username"));
            result.put("email", claims.get("email"));
            result.put("email_verified", claims.get("email_verified"));
            result.put("name", claims.get("name"));
            result.put("given_name", claims.get("given_name"));
            result.put("family_name", claims.get("family_name"));
            
            // Estrai i ruoli da realm_access
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                result.put("realm_roles", realmAccess.get("roles"));
            }

            // Estrai i ruoli da resource_access
            @SuppressWarnings("unchecked")
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            if (resourceAccess != null) {
                result.put("resource_access", resourceAccess);
            }

            // Aggiungi informazioni di scadenza
            if (claims.containsKey("exp")) {
                long exp = ((Number) claims.get("exp")).longValue();
                result.put("expires_at", exp);
                result.put("expires_at_readable", new java.util.Date(exp * 1000).toString());
            }

            if (claims.containsKey("iat")) {
                long iat = ((Number) claims.get("iat")).longValue();
                result.put("issued_at", iat);
                result.put("issued_at_readable", new java.util.Date(iat * 1000).toString());
            }

            // Aggiungi tutte le claims per debug
            result.put("all_claims", claims);

            LOGGER.info("Token decodificato con successo per l'utente: {}", claims.get("preferred_username"));
            
            return result;

        } catch (Exception e) {
            LOGGER.error("Errore durante la decodifica del token: {}", e.getMessage());
            throw new RuntimeException("Impossibile decodificare il token: " + e.getMessage(), e);
        }
    }

    /**
     * Estrae solo i ruoli del realm dal token
     * 
     * @param token Token JWT
     * @return Lista dei ruoli del realm
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRealmRoles(String token) {
        try {
            Map<String, Object> decoded = decodeToken(token);
            return (List<String>) decoded.get("realm_roles");
        } catch (Exception e) {
            LOGGER.error("Errore durante l'estrazione dei ruoli: {}", e.getMessage());
            throw new RuntimeException("Impossibile estrarre i ruoli dal token", e);
        }
    }

    /**
     * Verifica se il token è scaduto
     * 
     * @param token Token JWT
     * @return true se il token è scaduto, false altrimenti
     */
    public boolean isTokenExpired(String token) {
        try {
            Map<String, Object> decoded = decodeToken(token);
            if (decoded.containsKey("expires_at")) {
                long exp = ((Number) decoded.get("expires_at")).longValue();
                long now = System.currentTimeMillis() / 1000;
                return now > exp;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Errore durante la verifica della scadenza: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Estrae l'username dal token
     * 
     * @param token Token JWT
     * @return Username dell'utente
     */
    public String extractUsername(String token) {
        try {
            Map<String, Object> decoded = decodeToken(token);
            return (String) decoded.get("username");
        } catch (Exception e) {
            LOGGER.error("Errore durante l'estrazione dell'username: {}", e.getMessage());
            throw new RuntimeException("Impossibile estrarre l'username dal token", e);
        }
    }
}
