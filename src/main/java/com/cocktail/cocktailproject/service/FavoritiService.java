package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.UserFavorito;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.UserFavoritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FavoritiService - Gestione cocktail preferiti per utenti autenticati
 * 
 * Come funziona:
 * - L'ID utente (UUID) viene estratto dal JWT token di Keycloak (claim 'sub')
 * - Ogni utente può avere una lista personale di cocktail preferiti
 * - La relazione user-cocktail è Many-to-Many (un utente → molti cocktail preferiti)
 * - I favoriti sono persistiti nella tabella user_favoriti
 * 
 * Vincoli:
 * - Un utente non può aggiungere lo stesso cocktail due volte ai preferiti
 * - Se un cocktail viene eliminato, i favoriti associati vengono cancellati in cascata
 */
@Service
public class FavoritiService {
    
    @Autowired
    private UserFavoritoRepository favoritiRepository;
    
    @Autowired
    private CocktailRepository cocktailRepository;
    
    @Autowired
    private CocktailService cocktailService;
    
    /**
     * Aggiunge un cocktail ai favoriti dell'utente
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @param cocktailId ID del cocktail
     */
    @Transactional
    public void aggiungiPreferito(String keycloakUserId, Long cocktailId) {
        // Verifica che il cocktail esista
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + cocktailId));
        
        // Verifica se già nei favoriti
        if (favoritiRepository.existsByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId)) {
            throw new RuntimeException("Cocktail già presente nei favoriti");
        }
        
        // Crea e salva il favorito
        UserFavorito favorito = new UserFavorito();
        favorito.setKeycloakUserId(keycloakUserId);
        favorito.setCocktail(cocktail);
        favoritiRepository.save(favorito);
    }
    
    /**
     * Rimuove un cocktail dai favoriti
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @param cocktailId ID del cocktail
     */
    @Transactional
    public void rimuoviPreferito(String keycloakUserId, Long cocktailId) {
        UserFavorito favorito = favoritiRepository
                .findByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId)
                .orElseThrow(() -> new RuntimeException("Favorito non trovato"));
        
        favoritiRepository.delete(favorito);
    }
    
    /**
     * Ottiene tutti i cocktail preferiti di un utente
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @return Lista di cocktail preferiti
     */
    public List<CocktailDTO> getPreferiti(String keycloakUserId) {
        List<UserFavorito> favoriti = favoritiRepository.findByKeycloakUserId(keycloakUserId);
        
        return favoriti.stream()
                .map(fav -> cocktailService.getCocktailById(fav.getCocktail().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica se un cocktail è nei favoriti di un utente
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @param cocktailId ID del cocktail
     * @return true se è nei favoriti, false altrimenti
     */
    public boolean isPreferito(String keycloakUserId, Long cocktailId) {
        return favoritiRepository.existsByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId);
    }
    
    /**
     * Conta quanti favoriti ha un utente
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @return Numero di cocktail nei favoriti
     */
    public long contaPreferiti(String keycloakUserId) {
        return favoritiRepository.countByKeycloakUserId(keycloakUserId);
    }
    
    /**
     * Toggle: aggiunge il cocktail ai favoriti se non c'è, altrimenti lo rimuove
     * Ideale per frontend con pulsante on/off
     * 
     * @param keycloakUserId UUID utente da JWT (claim 'sub')
     * @param cocktailId ID del cocktail
     * @return true se aggiunto, false se rimosso
     */
    @Transactional
    public boolean togglePreferito(String keycloakUserId, Long cocktailId) {
        // Verifica che il cocktail esista
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + cocktailId));
        
        // Controlla se è già nei favoriti
        Optional<UserFavorito> esistente = favoritiRepository
                .findByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId);
        
        if (esistente.isPresent()) {
            // Rimuovi dai favoriti
            favoritiRepository.delete(esistente.get());
            return false;  // false = rimosso
        } else {
            // Aggiungi ai favoriti
            UserFavorito favorito = new UserFavorito();
            favorito.setKeycloakUserId(keycloakUserId);
            favorito.setCocktail(cocktail);
            favoritiRepository.save(favorito);
            return true;  // true = aggiunto
        }
    }
}
