package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Ingrediente - Elenco di ingredienti disponibili
 * Ogni nome è univoco (no duplicati)
 */
@Entity
@Table(name = "ingredienti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 150)
    private String nome;
}
