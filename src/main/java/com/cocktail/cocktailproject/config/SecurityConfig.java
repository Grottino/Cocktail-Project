package com.cocktail.cocktailproject.config;

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
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Catena di filtri per endpoint pubblici - NO OAuth2/JWT
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/auth/**", 
                        "/swagger-ui/**", 
                        "/swagger-ui.html",
                        "/v3/api-docs/**", 
                        "/api-docs/**", 
                        "/swagger-resources/**", 
                        "/webjars/**",
                        // Endpoint GET pubblici per cocktails (senza autenticazione)
                        "/api/cocktails",
                        "/api/cocktails/*",
                        "/api/cocktails/search"
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * Catena di filtri per endpoint protetti - CON OAuth2/JWT
     * Solo operazioni di scrittura (POST/PUT/DELETE) richiedono autenticazione
     */
    @Bean
    @Order(2)
    public SecurityFilterChain protectedSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Operazioni di scrittura riservate al ruolo SOLDIER (admin)
                        .requestMatchers(HttpMethod.POST, "/api/cocktails/**").hasRole("SOLDIER")
                        .requestMatchers(HttpMethod.PUT, "/api/cocktails/**").hasRole("SOLDIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").hasRole("SOLDIER")
                        // Favoriti: tutti gli utenti autenticati possono gestire i propri favoriti
                        .requestMatchers("/api/favoriti/**").authenticated()
                        // Tutto il resto richiede autenticazione
                        .anyRequest().authenticated())
                // OAuth2 Resource Server: valida JWT solo per questa catena
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    // Estrae i ruoli dal token JWT (da Keycloak)
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();
        rolesConverter.setAuthorityPrefix("ROLE_");
        rolesConverter.setAuthoritiesClaimName("realm_access.roles");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesConverter);
        return converter;
    }
}
