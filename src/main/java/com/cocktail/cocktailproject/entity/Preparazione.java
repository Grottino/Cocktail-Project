package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity Preparazione - Rappresenta uno STEP della ricetta di un cocktail
 * 
 * Tabella: preparazione (junction table tra cocktail e ingredienti con dati aggiuntivi)
 * 
 * Campi:
 * - id: Chiave primaria auto-incrementale
 * - cocktailId: FK verso cocktail (obbligatorio)
 * - ingredienteId: FK verso ingredienti (obbligatorio)
 * - quantita: Quantità numerica (DECIMAL 8,2)
 * - unita: Unità di misura (es: "ml", "oz", "cucchiaini", max 30 caratteri)
 * - stepOrder: Numero dello step nella sequenza (obbligatorio, determina l'ordine)
 * - istruzione: Istruzioni testuali per lo step (TEXT, opzionale)
 * 
 * Esempio per un Margarita:
 * Step 1: ingredienteId=1 (tequila), quantita=2.00, unita="oz", istruzione="Versare la tequila"
 * Step 2: ingredienteId=5 (lime juice), quantita=1.00, unita="oz", istruzione="Aggiungere lime"
 * Step 3: ingredienteId=7 (triple sec), quantita=0.50, unita="oz", istruzione="Aggiungere triple sec"
 * 
 * Relazioni:
 * - N:1 con Cocktail (molti step appartengono a un cocktail)
 * - N:1 con Ingrediente (molti step usano lo stesso ingrediente)
 * 
 * Cancellazione cascata: quando un cocktail viene eliminato, tutti i suoi step vengono cancellati.
 */
@Entity
@Table(name = "preparazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preparazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cocktail_id", nullable = false)
    private Long cocktailId;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    @Column(name = "quantita", precision = 8, scale = 2)
    private BigDecimal quantita;

    @Column(name = "unita", length = 30)
    private String unita;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "istruzione", columnDefinition = "TEXT")
    private String istruzione;
}
