package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.IngredientiDTO;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IngredientService - Logica di business per la gestione degli ingredienti.
 *
 * Responsabilità:
 * - Fornire operazioni di lettura (paginata) degli ingredienti
 * - Eseguire cancellazioni in sicurezza rimuovendo prima gli step di preparazione collegati
 * - Effettuare conversioni essenziali Entity -> DTO
 */
@Service
public class IngredientService {

    private final IngredienteRepository ingredienteRepository;
    private final PreparazioneRepository preparazioneRepository;

    /**
     * Costruttore con dependency injection dei repository.
     */
    public IngredientService(IngredienteRepository ingredienteRepository,
                             PreparazioneRepository preparazioneRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.preparazioneRepository = preparazioneRepository;
    }

    /**
     * Restituisce una pagina di ingredienti.
     * @param pageable parametri di paginazione
     * @return pagina di IngredientiDTO
     */
    public Page<IngredientiDTO> getAllIngredients(Pageable pageable) {
        return ingredienteRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Cerca ingredienti per nome con paginazione (ricerca parziale case-insensitive).
     * @param nome Stringa da cercare nel nome dell'ingrediente
     * @param pageable parametri di paginazione
     * @return pagina di IngredientiDTO che corrispondono alla ricerca
     */
    public Page<IngredientiDTO> searchByName(String nome, Pageable pageable) {
        return ingredienteRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Elimina un ingrediente per ID, rimuovendo prima i riferimenti negli step di preparazione.
     * @param id ID dell'ingrediente da eliminare
     * @return true se eliminato, false se non esistente
     */
    @Transactional
    public boolean deleteIngredient(Long id) {
        if (!ingredienteRepository.existsById(id)) {
            return false;
        }
        // Cancella gli step di preparazione che referenziano l'ingrediente, poi elimina l'ingrediente
        preparazioneRepository.deleteByIngredienteId(id);
        ingredienteRepository.deleteById(id);
        return true;
    }

    /**
     * Conversione minimale dell'entity Ingrediente in DTO.
     */
    private IngredientiDTO convertToDTO(Ingrediente ingrediente) {
        return new IngredientiDTO(ingrediente.getId(), ingrediente.getNome());
    }
}
