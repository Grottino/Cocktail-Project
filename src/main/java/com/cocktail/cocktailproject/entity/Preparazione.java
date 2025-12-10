package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
