package com.cocktail.cocktailproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDocConfig - Configurazione OpenAPI/Swagger
 * 
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cocktail API")
                .version("1.0.0")
                .description("API REST per la gestione di cocktail, ingredienti e preparazioni. " +
                           "Supporta operazioni CRUD complete e ricerca per nome.")
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
                    .description("Server di sviluppo locale"),
                new Server()
                    .url("http://localhost:8080")
                    .description("Server Docker")
            ));
    }
}
