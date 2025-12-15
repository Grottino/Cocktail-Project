package com.cocktail.cocktailproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRegistrationDTO - Dati per la registrazione di un nuovo utente
 * 
 * Utilizzato nell'endpoint POST /api/auth/register
 * Tutti i campi sono obbligatori e validati
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "Username è obbligatorio")
    @Size(min = 3, max = 50, message = "Username deve essere tra 3 e 50 caratteri")
    private String username;

    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email deve essere valida")
    private String email;

    @NotBlank(message = "Nome è obbligatorio")
    @Size(min = 1, max = 50, message = "Nome deve essere tra 1 e 50 caratteri")
    private String firstName;

    @NotBlank(message = "Cognome è obbligatorio")
    @Size(min = 1, max = 50, message = "Cognome deve essere tra 1 e 50 caratteri")
    private String lastName;

    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 8, message = "Password deve essere di almeno 8 caratteri")
    private String password;

    @NotBlank(message = "Conferma password è obbligatoria")
    private String confirmPassword;
}
