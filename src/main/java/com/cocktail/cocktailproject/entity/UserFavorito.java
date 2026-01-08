package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity UserFavorito - Rappresenta un cocktail nei preferiti di un utente
 * 
 * Tabella: user_favoriti (junction table per relazione Many-to-Many User-Cocktail)
 * 
 * Campi:
 * - id: Chiave primaria auto-incrementale
 * - keycloakUserId: UUID dell'utente estratto dal JWT Keycloak (claim 'sub')
 * - cocktail: Relazione ManyToOne verso il cocktail preferito
 * - createdAt: Timestamp di quando è stato aggiunto ai preferiti
 * 
 * Come funziona:
 * - Quando l'utente si autentica, il JWT contiene un claim 'sub' con l'UUID univoco
 * - L'UUID viene salvato nella colonna keycloak_user_id
 * - Non serve una tabella "users" nell'app: gli utenti vivono solo su Keycloak
 * 
 * Vincoli:
 * - Un utente non può aggiungere lo stesso cocktail due volte (gestito a livello service)
 * - Se un cocktail viene eliminato, i favoriti associati vengono cancellati in cascata
 * 
 * Relazioni:
 * - N:1 con Cocktail (molti favoriti puntano allo stesso cocktail)
 */
@Entity
@Table(name = "user_favoriti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "keycloak_user_id", nullable = false, length = 255)
    private String keycloakUserId;  // UUID dell'utente da JWT token (claim 'sub')
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
