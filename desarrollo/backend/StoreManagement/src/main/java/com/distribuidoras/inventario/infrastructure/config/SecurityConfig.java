package com.distribuidoras.inventario.infrastructure.config;

import com.distribuidoras.inventario.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/productos/**").hasAnyRole("SUPERVISOR_INVENTARIO", "ASESOR_COMERCIAL")
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ASESOR_COMERCIAL", "SUPERVISOR_INVENTARIO")
                        .requestMatchers("/api/v1/picking/**").hasRole("OPERARIO_PICKING")
                        .requestMatchers("/api/v1/despacho/**").hasRole("OPERARIO_DESPACHO")
                        .requestMatchers("/api/v1/recepcion/**").hasAnyRole("SUPERVISOR_INVENTARIO", "OPERARIO_PICKING")
                        .requestMatchers("/api/v1/manifiesto/**").hasAnyRole("SUPERVISOR_INVENTARIO", "ASESOR_COMERCIAL")
                        .requestMatchers("/api/v1/inventario/**").hasAnyRole("SUPERVISOR_INVENTARIO", "ASESOR_COMERCIAL")
                        .requestMatchers("/api/v1/excepcion/**").hasAnyRole("SUPERVISOR_INVENTARIO", "OPERARIO_PICKING", "OPERARIO_DESPACHO")
                        .requestMatchers("/api/v1/operarios/**").hasRole("SUPERVISOR_INVENTARIO")
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}