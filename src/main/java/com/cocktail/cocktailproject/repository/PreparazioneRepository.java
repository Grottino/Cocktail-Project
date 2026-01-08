package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Preparazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PreparazioneRepository - Accesso ai dati della tabella preparazione
 * 
 * Operazioni automatiche (da JpaRepository):
 * - findAll(): ottieni tutti gli step di preparazione
 * - save(preparazione): crea o aggiorna uno step
 * - deleteById(id): elimina uno step
 * 
 * Query custom:
 * - Ricerca step per cocktail con ordinamento
 * - Cancellazione di tutti gli step di un cocktail
 */
@Repository
public interface PreparazioneRepository extends JpaRepository<Preparazione, Long> {
    
    /**
     * Ottieni tutti gli step di preparazione di un cocktail, ordinati per numero step
     * @param cocktailId ID del cocktail
     * @return Lista di step ordinati (step 1, step 2, step 3, ...)
     */
    List<Preparazione> findByCocktailIdOrderByStepOrderAsc(Long cocktailId);
    
    /**
     * Elimina tutti gli step di preparazione di un cocktail
     * Usato prima di eliminare un cocktail (cancellazione cascata)
     * 
     * @param cocktailId ID del cocktail
     */
    void deleteByCocktailId(Long cocktailId);
}
