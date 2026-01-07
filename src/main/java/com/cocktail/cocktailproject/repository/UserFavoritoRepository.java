package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.UserFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per gestire i favoriti degli utenti
 */
@Repository
public interface UserFavoritoRepository extends JpaRepository<UserFavorito, Long> {
    
    /**
     * Trova tutti i favoriti di un utente specifico
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     */
    List<UserFavorito> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Verifica se un cocktail è già nei favoriti di un utente
     */
    boolean existsByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Trova un favorito specifico (utente + cocktail)
     */
    Optional<UserFavorito> findByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Conta quanti favoriti ha un utente
     */
    long countByKeycloakUserId(String keycloakUserId);
    
    /**
     * Elimina tutti i favoriti associati a un cocktail
     * Utilizzato prima di eliminare un cocktail
     */
    void deleteByCocktailId(Long cocktailId);
}
