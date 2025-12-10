package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.service.CocktailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cocktails")
@CrossOrigin(origins = "*")
public class CocktailController {

    @Autowired
    private CocktailService cocktailService;

    /**
     * GET /api/cocktails - Ottiene tutti i cocktail
     */
    @GetMapping
    public ResponseEntity<List<CocktailDTO>> getAllCocktails() {
        List<CocktailDTO> cocktails = cocktailService.getAllCocktails();
        return ResponseEntity.ok(cocktails);
    }

    /**
     * GET /api/cocktails/{id} - Ottiene un cocktail per ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CocktailDTO> getCocktailById(@PathVariable Long id) {
        Optional<CocktailDTO> cocktail = cocktailService.getCocktailById(id);
        return cocktail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/cocktails/search?nome=xxx - Cerca cocktail per nome
     */
    @GetMapping("/search")
    public ResponseEntity<List<CocktailDTO>> searchByName(@RequestParam String nome) {
        List<CocktailDTO> cocktails = cocktailService.searchByName(nome);
        return ResponseEntity.ok(cocktails);
    }

    /**
     * POST /api/cocktails - Crea un nuovo cocktail
     */
    @PostMapping
    public ResponseEntity<CocktailDTO> createCocktail(@RequestBody CocktailDTO cocktailDTO) {
        CocktailDTO created = cocktailService.createCocktail(cocktailDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/cocktails/{id} - Aggiorna un cocktail
     */
    @PutMapping("/{id}")
    public ResponseEntity<CocktailDTO> updateCocktail(@PathVariable Long id, @RequestBody CocktailDTO cocktailDTO) {
        Optional<CocktailDTO> updated = cocktailService.updateCocktail(id, cocktailDTO);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/cocktails/{id} - Elimina un cocktail
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCocktail(@PathVariable Long id) {
        if (cocktailService.deleteCocktail(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
