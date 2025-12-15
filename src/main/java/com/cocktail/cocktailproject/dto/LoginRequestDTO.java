package com.cocktail.cocktailproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequestDTO - Dati per la richiesta di login
 * 
 * Utilizzato nell'endpoint POST /api/auth/login
 * Username e password sono obbligatori
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Username è obbligatorio")
    private String username;

    @NotBlank(message = "Password è obbligatoria")
    private String password;
}
