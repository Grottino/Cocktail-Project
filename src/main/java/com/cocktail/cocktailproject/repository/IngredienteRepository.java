package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * IngredienteRepository - Accesso ai dati degli ingredienti
 * Operazioni CRUD automatiche + metodi custom
 */
@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    // Cerca ingrediente per nome esatto
    Optional<Ingrediente> findByNome(String nome);
    
    // Cerca ingrediente per nome ignorando case sensitivity
    Optional<Ingrediente> findByNomeIgnoreCase(String nome);
}
