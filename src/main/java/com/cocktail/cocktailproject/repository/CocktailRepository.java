package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CocktailRepository - Accesso ai dati dei cocktail
 * Operazioni CRUD automatiche + metodi custom
 */
@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    // CRUD espliciti (già forniti da JpaRepository, dichiarati per chiarezza)
    List<Cocktail> findAll();
    Optional<Cocktail> findById(Long id);
    Cocktail save(Cocktail cocktail);
    boolean existsById(Long id);
    void deleteById(Long id);

    // Query custom
    List<Cocktail> findByNomeContainingIgnoreCase(String nome);
}
