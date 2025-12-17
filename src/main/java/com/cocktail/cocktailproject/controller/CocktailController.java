package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.dto.CreateCocktailRequestDTO;
import com.cocktail.cocktailproject.service.CocktailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cocktails")
@CrossOrigin(origins = "*")
@Tag(name = "Cocktail", description = "API per la gestione dei cocktail")
public class CocktailController {

    @Autowired
    private CocktailService cocktailService;

    /**
     * GET /api/cocktails - Ottiene tutti i cocktail
     */
    @Operation(summary = "Ottieni tutti i cocktail", description = "Restituisce una lista di tutti i cocktail disponibili nel sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista di cocktail recuperata con successo",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CocktailDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<CocktailDTO>> getAllCocktails() {
        List<CocktailDTO> cocktails = cocktailService.getAllCocktails();
        return ResponseEntity.ok(cocktails);
    }

    /**
     * GET /api/cocktails/{id} - Ottiene un cocktail per ID
     */
    @Operation(summary = "Ottieni cocktail per ID", description = "Restituisce un cocktail specifico basato sul suo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocktail trovato e restituito"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CocktailDTO> getCocktailById(
            @Parameter(description = "ID del cocktail da recuperare")
            @PathVariable Long id) {
        Optional<CocktailDTO> cocktail = cocktailService.getCocktailById(id);
        return cocktail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/cocktails/search?nome=xxx - Cerca cocktail per nome
     */
    @Operation(summary = "Cerca cocktail per nome", description = "Restituisce una lista di cocktail che corrispondono al nome ricercato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ricerca completata, lista di cocktail restituita")
    })
    @GetMapping("/search")
    public ResponseEntity<List<CocktailDTO>> searchByName(
            @Parameter(description = "Nome del cocktail da cercare")
            @RequestParam String nome) {
        List<CocktailDTO> cocktails = cocktailService.searchByName(nome);
        return ResponseEntity.ok(cocktails);
    }

    /**
     * POST /api/cocktails - Crea un nuovo cocktail con ingredienti e preparazione
     * 
     * Esempio request:
     * {
     *   "nome": "Margarita",
     *   "descrizione": "Cocktail messicano",
     *   "tempoPreparazioneMinutes": 5,
     *   "ingredienti": [
     *     { "nome": "tequila", "quantita": 2, "unita": "oz" },
     *     { "nome": "lime juice", "quantita": 1, "unita": "oz" },
     *     { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
     *   ],
     *   "preparazione": "Versare in shaker con ghiaccio e agitare"
     * }
     */
        @PreAuthorize("hasRole('SOLDIER')")
    @Operation(
            summary = "Crea un nuovo cocktail con ingredienti",
            description = "Crea un nuovo cocktail con almeno 2 ingredienti. Gli ingredienti non esistenti vengono creati automaticamente.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cocktail creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi - minimo 2 ingredienti richiesti")
    })
    @PostMapping
    public ResponseEntity<CocktailDTO> createCocktail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati del cocktail con ingredienti", required = true)
            @RequestBody CreateCocktailRequestDTO requestDTO) {
        try {
            CocktailDTO created = cocktailService.createCocktail(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/cocktails/{id} - Aggiorna un cocktail
     */
        @PreAuthorize("hasRole('SOLDIER')")
    @Operation(
            summary = "Aggiorna un cocktail",
            description = "Aggiorna i dati di un cocktail esistente",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocktail aggiornato con successo"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CocktailDTO> updateCocktail(
            @Parameter(description = "ID del cocktail da aggiornare")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati aggiornati del cocktail", required = true)
            @RequestBody CocktailDTO cocktailDTO) {
        Optional<CocktailDTO> updated = cocktailService.updateCocktail(id, cocktailDTO);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/cocktails/{id} - Elimina un cocktail
     */
        @PreAuthorize("hasRole('SOLDIER')")
    @Operation(
            summary = "Elimina un cocktail",
            description = "Elimina un cocktail dal sistema",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cocktail eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCocktail(
            @Parameter(description = "ID del cocktail da eliminare")
            @PathVariable Long id) {
        if (cocktailService.deleteCocktail(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
