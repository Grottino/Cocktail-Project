package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Preparazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PreparazioneRepository - Accesso ai dati degli step di preparazione
 * Operazioni CRUD automatiche + metodi custom
 */
@Repository
public interface PreparazioneRepository extends JpaRepository<Preparazione, Long> {
    // Ottieni gli step di un cocktail, ordinati per numero
    List<Preparazione> findByCocktailIdOrderByStepOrderAsc(Long cocktailId);
    
    // Elimina tutti gli step di un cocktail
    void deleteByCocktailId(Long cocktailId);
}
