package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Preparazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreparazioneRepository extends JpaRepository<Preparazione, Long> {
    List<Preparazione> findByCocktailIdOrderByStepOrderAsc(Long cocktailId);
    void deleteByCocktailId(Long cocktailId);
}
