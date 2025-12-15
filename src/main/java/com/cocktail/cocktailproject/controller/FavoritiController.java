package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.service.FavoritiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller per gestire i cocktail preferiti degli utenti
 * Tutte le operazioni richiedono autenticazione JWT
 * L'ID utente viene automaticamente estratto dal token JWT di Keycloak (claim 'sub')
 */
@RestController
@RequestMapping("/api/favoriti")
@CrossOrigin(origins = "*")
@Tag(name = "Favoriti", description = "API per gestire i cocktail preferiti dell'utente autenticato")
@SecurityRequirement(name = "bearerAuth")
public class FavoritiController {
    
    @Autowired
    private FavoritiService favoritiService;
    
    /**
     * POST /api/favoriti/{cocktailId} - Aggiungi un cocktail ai favoriti
     */
    @Operation(
        summary = "Aggiungi cocktail ai favoriti", 
        description = "Richiede autenticazione JWT. Aggiunge un cocktail ai preferiti dell'utente autenticato. " +
                     "L'ID utente viene automaticamente estratto dal token JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cocktail aggiunto ai favoriti con successo"),
        @ApiResponse(responseCode = "400", description = "Cocktail già nei favoriti o ID non valido"),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante"),
        @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @PostMapping("/{cocktailId}")
    public ResponseEntity<Map<String, Object>> aggiungiPreferito(
            @Parameter(description = "ID del cocktail da aggiungere ai favoriti")
            @PathVariable Long cocktailId,
            Authentication authentication) {
        
        String userId = getUserIdFromToken(authentication);
        
        try {
            favoritiService.aggiungiPreferito(userId, cocktailId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cocktail aggiunto ai favoriti");
            response.put("cocktailId", cocktailId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * DELETE /api/favoriti/{cocktailId} - Rimuovi un cocktail dai favoriti
     */
    @Operation(
        summary = "Rimuovi cocktail dai favoriti",
        description = "Richiede autenticazione JWT. Rimuove un cocktail dai preferiti dell'utente autenticato."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cocktail rimosso dai favoriti con successo"),
        @ApiResponse(responseCode = "400", description = "Favorito non trovato"),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante")
    })
    @DeleteMapping("/{cocktailId}")
    public ResponseEntity<Map<String, Object>> rimuoviPreferito(
            @Parameter(description = "ID del cocktail da rimuovere dai favoriti")
            @PathVariable Long cocktailId,
            Authentication authentication) {
        
        String userId = getUserIdFromToken(authentication);
        
        try {
            favoritiService.rimuoviPreferito(userId, cocktailId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cocktail rimosso dai favoriti");
            response.put("cocktailId", cocktailId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * GET /api/favoriti - Ottieni tutti i cocktail preferiti
     */
    @Operation(
        summary = "Ottieni i cocktail preferiti",
        description = "Richiede autenticazione JWT. Restituisce la lista completa dei cocktail nei favoriti dell'utente autenticato."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista dei cocktail preferiti recuperata con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CocktailDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante")
    })
    @GetMapping
    public ResponseEntity<List<CocktailDTO>> getPreferiti(Authentication authentication) {
        String userId = getUserIdFromToken(authentication);
        List<CocktailDTO> preferiti = favoritiService.getPreferiti(userId);
        return ResponseEntity.ok(preferiti);
    }
    
    /**
     * GET /api/favoriti/check/{cocktailId} - Verifica se un cocktail è nei favoriti
     */
    @Operation(
        summary = "Verifica se un cocktail è nei favoriti",
        description = "Richiede autenticazione JWT. Controlla se un cocktail specifico è già nei favoriti dell'utente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verifica completata con successo"),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante")
    })
    @GetMapping("/check/{cocktailId}")
    public ResponseEntity<Map<String, Boolean>> checkPreferito(
            @Parameter(description = "ID del cocktail da verificare")
            @PathVariable Long cocktailId,
            Authentication authentication) {
        
        String userId = getUserIdFromToken(authentication);
        boolean isPreferito = favoritiService.isPreferito(userId, cocktailId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorite", isPreferito);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/favoriti/count - Conta i favoriti dell'utente
     */
    @Operation(
        summary = "Conta i cocktail preferiti",
        description = "Richiede autenticazione JWT. Restituisce il numero totale di cocktail nei favoriti."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conteggio completato con successo"),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contaPreferiti(Authentication authentication) {
        String userId = getUserIdFromToken(authentication);
        long count = favoritiService.contaPreferiti(userId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/favoriti/toggle/{cocktailId} - Toggle favorito (aggiungi/rimuovi)
     */
    @Operation(
        summary = "Toggle cocktail nei favoriti", 
        description = "Richiede autenticazione JWT. Se il cocktail è già nei favoriti lo rimuove, " +
                     "altrimenti lo aggiunge. Perfetto per un pulsante on/off nel frontend."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Toggle completato con successo"),
        @ApiResponse(responseCode = "401", description = "Token JWT non valido o mancante"),
        @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @PostMapping("/toggle/{cocktailId}")
    public ResponseEntity<Map<String, Object>> togglePreferito(
            @Parameter(description = "ID del cocktail da aggiungere/rimuovere dai favoriti")
            @PathVariable Long cocktailId,
            Authentication authentication) {
        
        String userId = getUserIdFromToken(authentication);
        
        try {
            boolean isAggiunto = favoritiService.togglePreferito(userId, cocktailId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", isAggiunto);
            response.put("message", isAggiunto ? "Cocktail aggiunto ai favoriti" : "Cocktail rimosso dai favoriti");
            response.put("cocktailId", cocktailId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Estrae l'ID utente (UUID) dal token JWT di Keycloak
     * Il claim 'sub' (subject) contiene l'UUID univoco dell'utente
     * 
     * @param authentication Oggetto di autenticazione Spring Security
     * @return UUID utente Keycloak (claim 'sub')
     */
    private String getUserIdFromToken(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();  // Claim "sub" = UUID utente Keycloak
    }
}
