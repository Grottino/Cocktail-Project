package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    List<Cocktail> findByNomeContainingIgnoreCase(String nome);
}
