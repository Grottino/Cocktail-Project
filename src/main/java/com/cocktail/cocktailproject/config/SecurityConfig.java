package com.cocktail.cocktailproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * SecurityConfig - Due catene di filtri separate:
 * 
 * 1. publicSecurityFilterChain (Order 1) - Nessuna autenticazione JWT:
 *    - /api/auth/** (login, register)
 *    - Swagger UI
 * 
 * 2. protectedSecurityFilterChain (Order 2) - Autenticazione JWT obbligatoria:
 *    - GET /api/cocktails (pubblica)
 *    - POST/PUT/DELETE /api/cocktails → Solo admin
 */@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                //  ENDPOINT PUBBLICI
                .requestMatchers(
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // GET cocktails PUBBLICI
                .requestMatchers(HttpMethod.GET, "/api/cocktails/**").permitAll()

                // POST cocktails PROTETTA (utenti autenticati)
                .requestMatchers(HttpMethod.POST, "/api/cocktails/**").authenticated()

                //  ENDPOINT PROTETTI (Admin)
                .requestMatchers(HttpMethod.PUT, "/api/cocktails/**").hasRole("SOLDIER")
                .requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").hasRole("SOLDIER")

                // GET ingredients PUBBLICI
                .requestMatchers(HttpMethod.GET, "/api/ingredients/**").permitAll()
                // DELETE ingredients PROTETTA (utenti autenticati)
                .requestMatchers(HttpMethod.DELETE, "/api/ingredients/**").hasRole("SOLDIER")

                // Favoriti → utenti autenticati
                .requestMatchers("/api/favoriti/**").authenticated()

                // Tutto il resto richiede autenticazione
                .anyRequest().authenticated()
            )

            //  JWT (Keycloak)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    // Estrazione ruoli dal JWT (Keycloak)
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Converter personalizzato per estrarre ruoli da realm_access.roles
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                var roles = (java.util.Collection<String>) realmAccess.get("roles");
                return roles.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                        .collect(java.util.stream.Collectors.toList());
            }
            return java.util.Collections.emptyList();
        });

        return converter;
    }

    // Bean per ObjectMapper - configurato per ignorare campi sconosciuti
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
        return mapper;
    }

    // Bean per RestTemplate - usato per chiamate HTTP a Keycloak
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

