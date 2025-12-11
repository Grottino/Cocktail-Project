package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Cocktail - Rappresenta un cocktail nel database
 * Contiene nome, descrizione e tempo di preparazione
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
