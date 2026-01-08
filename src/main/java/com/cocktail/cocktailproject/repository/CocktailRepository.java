package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Cocktail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CocktailRepository - Accesso ai dati della tabella cocktail
 * 
 * Operazioni automatiche (da JpaRepository):
 * - findAll(): ottieni tutti i cocktail
 * - findById(id): ottieni cocktail per ID
 * - save(cocktail): crea o aggiorna un cocktail
 * - deleteById(id): elimina un cocktail
 * - existsById(id): verifica esistenza
 * 
 * Query custom:
 * - Ricerca per nome con supporto paginazione
 */
@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    
    /**
     * Cerca cocktail per nome (case-insensitive, ricerca parziale) - senza paginazione
     * @param nome Stringa da cercare (es: "marg" trova "Margarita")
     * @return Lista di tutti i cocktail che contengono la stringa nel nome
     */
    List<Cocktail> findByNomeContainingIgnoreCase(String nome);
    
    /**
     * Cerca cocktail per nome con paginazione (metodo raccomandato)
     * @param nome Stringa da cercare (case-insensitive, ricerca parziale)
     * @param pageable Parametri di paginazione (page, size, sort)
     * @return Pagina di risultati
     */
    Page<Cocktail> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
