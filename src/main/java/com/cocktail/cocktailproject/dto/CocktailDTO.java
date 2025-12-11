package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CocktailDTO - Data Transfer Object per i Cocktail
 * 
 * Usato per trasferire dati tra il Controller e i Client (HTTP).
 * Combina i dati di Cocktail con una lista formattata di StepPreparazioneDTO.
 * DTO = serve a nascondere dettagli del database e presentare dati puliti/formattatati al client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CocktailDTO {
    private Long id;
    private String nome;
    private String descrizione;
    private Integer tempoPreparazioneMinutes;
    private String note;
    private List<StepPreparazioneDTO> preparazione;

    /**
     * StepPreparazioneDTO - Uno step della ricetta
     * Es: 25ml Gin, 2 oz tequila, ecc.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepPreparazioneDTO {
        private Integer stepOrder;
        private String ingrediente;
        private String quantita;
        private String unita;
        private String istruzione;
    }
}
