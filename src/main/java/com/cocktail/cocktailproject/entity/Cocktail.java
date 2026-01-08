package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Cocktail - Rappresenta un cocktail nel database
 * 
 * Tabella: cocktail
 * 
 * Campi:
 * - id: Chiave primaria auto-incrementale
 * - nome: Nome del cocktail (max 150 caratteri, obbligatorio)
 * - descrizione: Descrizione dettagliata (TEXT, opzionale)
 * - tempoPreparazioneMinutes: Tempo di preparazione in minuti (opzionale)
 * - note: Note aggiuntive (TEXT, opzionale)
 * 
 * Relazioni:
 * - 1:N con Preparazione (un cocktail ha molti step di preparazione)
 * - M:N con Ingrediente (attraverso la tabella Preparazione)
 * - 1:N con UserFavorito (un cocktail può essere nei preferiti di molti utenti)
 */
@Entity
@Table(name = "cocktail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cocktail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "tempo_preparazione_minutes")
    private Integer tempoPreparazioneMinutes;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
