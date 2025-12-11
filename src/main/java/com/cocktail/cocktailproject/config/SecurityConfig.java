package com.cocktail.cocktailproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Permessi e autenticazione OAuth2/JWT
 * 
 * GET /api/cocktails → Pubblico
 * POST/PUT/DELETE /api/cocktails → Solo admin
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/cocktails",
                                "/api/cocktails/**",
                                "/api/cocktails/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cocktails/**").permitAll() // TODO: cambia in hasRole("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/cocktails/**").permitAll()  // TODO: cambia in hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").permitAll() // TODO: cambia in hasRole("admin")
                        .anyRequest().permitAll());
                // .oauth2ResourceServer(oauth2 -> oauth2  // DISABILITATO TEMPORANEAMENTE
                //         .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

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
