package com.cocktail.cocktailproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SecurityConfig - Due catene di filtri separate:
 * 
 * 1. publicSecurityFilterChain (Order 1) - Nessuna autenticazione JWT:
 *    - /api/auth/** (login, register)
 *    - Swagger UI
 * 
 * 2. protectedSecurityFilterChain (Order 2) - Autenticazione JWT obbligatoria:
 *    - GET /api/cocktails (pubblica)
 *    - POST/PUT/DELETE /api/cocktails → Solo SOLDIER
 * 
 * Il converter JWT personalizzato estrae i ruoli da realm_access.roles del token Keycloak
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
                "/v3/api-docs/**",
                "/swagger-ui.html"
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Catena di filtri per endpoint protetti - Richiede JWT OAuth2
     */
    @Bean
    @Order(2)
    public SecurityFilterChain protectedSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // GET pubblico
                .requestMatchers(HttpMethod.GET, "/api/cocktails/**").permitAll()
                
                // POST, PUT, DELETE richiedono ruolo SOLDIER
                .requestMatchers(HttpMethod.POST, "/api/cocktails/**").hasRole("SOLDIER")
                .requestMatchers(HttpMethod.PUT, "/api/cocktails/**").hasRole("SOLDIER")
                .requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").hasRole("SOLDIER")
                
                // Favoriti richiedono autenticazione (USER o SOLDIER)
                .requestMatchers("/api/favoriti/**").authenticated()
                
                // Tutti gli altri endpoint protetti richiedono autenticazione
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Converter personalizzato per estrarre i ruoli dal token JWT di Keycloak
     * 
     * Keycloak inserisce i ruoli in:
     * - realm_access.roles: ruoli del realm (es: SOLDIER, USER, SOLDIER)
     * - resource_access.<client>.roles: ruoli specifici del client
     * 
     * Spring Security richiede il prefisso "ROLE_" per i ruoli.
     * Esempio: "SOLDIER" nel JWT diventa "ROLE_SOLDIER" in Spring Security
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        // Converter personalizzato per estrarre i ruoli da realm_access
        converter.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                // Estrai i ruoli da realm_access.roles
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                
                if (realmAccess == null || !realmAccess.containsKey("roles")) {
                    return Collections.emptyList();
                }
                
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                
                // Converti i ruoli in GrantedAuthority con prefisso ROLE_
                return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
            }
        });
        
        return converter;
    }
}