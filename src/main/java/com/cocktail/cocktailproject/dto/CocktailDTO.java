package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
