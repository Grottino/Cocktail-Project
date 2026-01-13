package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.IngredientiDTO;
import com.cocktail.cocktailproject.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * IngredientController - Endpoint REST per la gestione degli ingredienti.
 *
 * Responsabilità:
 * - Espone API separate dagli endpoint dei cocktail
 * - Fornisce operazioni di lettura e cancellazione degli ingredienti
 * - Usa la paginazione per evitare payload troppo grandi
 */
@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "*")
@Tag(name = "Ingredient", description = "API per la gestione degli ingredienti")
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * Costruttore con injection del service.
     */
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * GET /api/ingredients - Restituisce una lista paginata di ingredienti.
     */
    @Operation(summary =  "Ottieni tutti gli ingredienti disponibili", description = "Restituisce una lista paginata di tutti gli ingredienti presenti nel sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginata ingredienti restituita con successo")
    })
    @GetMapping
    public ResponseEntity<Page<IngredientiDTO>> getAllIngredients(
            @Parameter(description = "Numero della pagina (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Numero di ingredienti per pagina (default 10)")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<IngredientiDTO> ingredients = ingredientService.getAllIngredients(pageable);
        return ResponseEntity.ok(ingredients);
    }

    /**
     * GET /api/ingredients/search?nome=xxx - Cerca ingredienti per nome con paginazione.
     */
    @Operation(summary = "Cerca ingredienti per nome", description = "Restituisce una lista paginata di ingredienti che corrispondono al nome ricercato (ricerca parziale case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ricerca completata, lista paginata di ingredienti restituita")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<IngredientiDTO>> searchByName(
            @Parameter(description = "Nome dell'ingrediente da cercare")
            @RequestParam String nome,
            @Parameter(description = "Numero della pagina (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Numero di ingredienti per pagina (default 10)")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<IngredientiDTO> ingredients = ingredientService.searchByName(nome, pageable);
        return ResponseEntity.ok(ingredients);
    }

    /**
     * DELETE /api/ingredients/{id} - Elimina l'ingrediente e i riferimenti negli step di preparazione.
     */
    @Operation(summary =  "Elimina un ingrediente", description = "Elimina un ingrediente dal sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ingrediente eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Ingrediente non trovato")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(
            @Parameter(description = "ID dell'ingrediente da eliminare")
            @PathVariable Long id) {
        if (ingredientService.deleteIngredient(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
