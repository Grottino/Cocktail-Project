package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.UserFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserFavoritoRepository - Accesso ai dati della tabella user_favoriti
 * 
 * Gestisce la relazione Many-to-Many tra utenti (Keycloak) e cocktail.
 * L'ID utente è l'UUID estratto dal JWT token (claim 'sub').
 * 
 * Operazioni automatiche (da JpaRepository):
 * - findAll(): ottieni tutti i favoriti di tutti gli utenti
 * - save(favorito): aggiungi un nuovo favorito
 * - delete(favorito): rimuovi un favorito
 * 
 * Query custom:
 * - Ricerca favoriti per utente
 * - Verifica esistenza favorito
 * - Conteggio favoriti per utente
 * - Cancellazione per cocktail
 */
@Repository
public interface UserFavoritoRepository extends JpaRepository<UserFavorito, Long> {
    
    /**
     * Trova tutti i cocktail preferiti di un utente
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @return Lista di tutti i favoriti dell'utente
     */
    List<UserFavorito> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Verifica se un cocktail è già nei favoriti di un utente
     * Utile per prevenire duplicati e per mostrare lo stato "preferito" nel frontend
     * 
     * @param keycloakUserId UUID utente da JWT
     * @param cocktailId ID del cocktail da verificare
     * @return true se è già nei favoriti, false altrimenti
     */
    boolean existsByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Trova un favorito specifico (combinazione utente + cocktail)
     * Usato per rimuovere un favorito esistente
     * 
     * @param keycloakUserId UUID utente da JWT
     * @param cocktailId ID del cocktail
     * @return Optional contenente il favorito se esiste
     */
    Optional<UserFavorito> findByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Conta quanti cocktail ha nei preferiti un utente
     * Utile per badge/contatori nel frontend
     * 
     * @param keycloakUserId UUID utente da JWT
     * @return Numero totale di favoriti dell'utente
     */
    long countByKeycloakUserId(String keycloakUserId);
    
    /**
     * Elimina tutti i favoriti associati a un cocktail
     * 
     * Chiamato automaticamente prima di eliminare un cocktail (cancellazione cascata).
     * Previene constraint violation sul foreign key.
     * 
     * @param cocktailId ID del cocktail da cui rimuovere tutti i favoriti
     */
    void deleteByCocktailId(Long cocktailId);
}
