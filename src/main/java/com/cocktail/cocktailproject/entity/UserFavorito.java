package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity UserFavorito - Rappresenta un cocktail preferito di un utente
 * L'ID utente è l'UUID estratto dal token JWT di Keycloak (claim 'sub')
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
