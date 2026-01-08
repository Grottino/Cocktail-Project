package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Cocktail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CocktailRepository - Accesso ai dati dei cocktail
 * Operazioni CRUD automatiche + metodi custom
 */
@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    // Cerca cocktail per nome (case-insensitive) - versione legacy
    List<Cocktail> findByNomeContainingIgnoreCase(String nome);
    
    // Cerca cocktail per nome con paginazione
    Page<Cocktail> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
