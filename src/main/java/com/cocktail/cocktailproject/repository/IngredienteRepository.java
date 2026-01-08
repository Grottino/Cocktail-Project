package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * IngredienteRepository - Accesso ai dati della tabella ingredienti
 * 
 * Operazioni automatiche (da JpaRepository):
 * - findAll(): ottieni tutti gli ingredienti
 * - findById(id): ottieni ingrediente per ID
 * - save(ingrediente): crea o aggiorna un ingrediente
 * - deleteById(id): elimina un ingrediente
 * 
 * Query custom:
 * - Ricerca per nome esatto e case-insensitive
 */
@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    
    /**
     * Cerca ingrediente per nome esatto (case-sensitive)
     * @param nome Nome da cercare (deve corrispondere esattamente)
     * @return Optional contenente l'ingrediente se trovato
     */
    Optional<Ingrediente> findByNome(String nome);
    
    /**
     * Cerca ingrediente per nome ignorando maiuscole/minuscole (raccomandato)
     * @param nome Nome da cercare ("gin" trova "Gin", "GIN", "gin")
     * @return Optional contenente l'ingrediente se trovato
     */
    Optional<Ingrediente> findByNomeIgnoreCase(String nome);
}
