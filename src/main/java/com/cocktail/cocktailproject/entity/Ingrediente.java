package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Ingrediente - Catalogo degli ingredienti disponibili nel sistema
 * 
 * Tabella: ingredienti
 * 
 * Campi:
 * - id: Chiave primaria auto-incrementale
 * - nome: Nome ingrediente (max 150 caratteri, obbligatorio, UNIQUE)
 * 
 * Vincoli:
 * - Il nome deve essere univoco (non possono esistere due ingredienti con lo stesso nome)
 * - Normalizzazione: i nomi vengono salvati in lowercase per evitare duplicati
 * 
 * Relazioni:
 * - 1:N con Preparazione (un ingrediente può essere usato in molti cocktail)
 * 
 * Nota: Gli ingredienti NON vengono eliminati quando un cocktail viene cancellato,
 * perché possono essere riutilizzati in altri cocktail.
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
