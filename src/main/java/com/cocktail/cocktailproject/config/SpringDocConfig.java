package com.cocktail.cocktailproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDocConfig - Configurazione OpenAPI/Swagger con Bearer Token Authentication
 * 
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * Per autenticarsi:
 * 1. Chiama l'endpoint POST /api/auth/login con username e password
 * 2. Copia l'access_token dalla risposta
 * 3. Clicca "Authorize" in Swagger UI
 * 4. Incolla il token (senza prefisso "Bearer")
 * 5. Il token verrà automaticamente incluso in tutte le richieste protette
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("Cocktail API")
                .version("1.0.0")
                .description("API REST per la gestione di cocktail, ingredienti e preparazioni. " +
                           "Supporta operazioni CRUD complete e ricerca per nome.\n\n" +
                           "**Come Autenticarsi:**\n\n" +
                           "1. **Effettua il Login:**\n" +
                           "   - Vai all'endpoint `POST /api/auth/login`\n" +
                           "   - Clicca 'Try it out'\n" +
                           "   - Inserisci le credenziali:\n" +
                           "     ```json\n" +
                           "     {\n" +
                           "       \"username\": \"soldier\",\n" +
                           "       \"password\": \"soldier\"\n" +
                           "     }\n" +
                           "     ```\n" +
                           "   - Clicca 'Execute'\n" +
                           "   - Copia l'`access_token` dalla risposta\n\n" +
                           "2. **Autorizza Swagger:**\n" +
                           "   - Clicca il pulsante 'Authorize' 🔓 in alto a destra\n" +
                           "   - Incolla il token nel campo 'Value'\n" +
                           "   - Clicca 'Authorize' e poi 'Close'\n\n" +
                           "3. **Usa le API Protette:**\n" +
                           "   - Ora puoi chiamare tutti gli endpoint protetti\n" +
                           "   - Il token verrà incluso automaticamente nell'header `Authorization: Bearer {token}`\n\n" +
                           "**Utenti di Test:**\n" +
                           "- `user` / password configurata (ruolo: user - solo lettura)\n" +
                           "- `soldier` / password configurata (ruolo: admin - tutti i permessi)\n\n" +
                           "**Registrazione:**\n" +
                           "- Puoi creare nuovi utenti con l'endpoint `POST /api/auth/register`")
                .contact(new Contact()
                    .name("Cocktail Project Team")
                    .url("http://localhost:8080")
                    .email("info@cocktail-project.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Server di sviluppo locale")
            ))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Inserisci il JWT token ottenuto dall'endpoint /api/auth/login. " +
                               "Non serve aggiungere 'Bearer ' come prefisso, viene aggiunto automaticamente.")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
