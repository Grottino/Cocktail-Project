package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CreateCocktailRequestDTO - DTO per la creazione di un nuovo cocktail
 * 
 * Contiene:
 * - Dati del cocktail (nome, descrizione, tempo, note)
 * - Lista di ingredienti con quantità e unità
 * - Preparazione (opzionale - default: "Mescolare gli ingredienti")
 * 
 * Esempio JSON:
 * {
 *   "nome": "Margarita",
 *   "descrizione": "Cocktail classico messicano",
 *   "tempoPreparazioneMinutes": 5,
 *   "note": "Servire freddo",
 *   "ingredienti": [
 *     { "nome": "tequila", "quantita": 2, "unita": "oz" },
 *     { "nome": "lime juice", "quantita": 1, "unita": "oz" },
 *     { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
 *   ],
 *   "preparazione": "Versare in un shaker con ghiaccio. Agitare e versare in un bicchiere con ghiaccio"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCocktailRequestDTO {
    
    private String nome;
    private String descrizione;
    private Integer tempoPreparazioneMinutes;
    private String note;
    private List<IngredienteStepDTO> ingredienti;
    private String preparazione;
    
    /**
     * IngredienteStepDTO - Rappresenta un ingrediente con quantità per il cocktail
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredienteStepDTO {
        private String nome;
        private BigDecimal quantita;
        private String unita;
    }
}
