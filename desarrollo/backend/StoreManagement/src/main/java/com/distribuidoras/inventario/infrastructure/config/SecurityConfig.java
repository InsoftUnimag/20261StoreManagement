package com.distribuidoras.inventario.infrastructure.config;

import com.distribuidoras.inventario.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").authenticated()
                        .requestMatchers("/api/v1/productos/**").hasRole("SUPERVISOR_INVENTARIO")
                        
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos", "/api/v1/pedidos/comprometidos").hasAnyRole("ASESOR_COMERCIAL", "SUPERVISOR_INVENTARIO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/picking/pedidos", "/api/v1/despacho/pedidos").hasRole("SUPERVISOR_INVENTARIO")
                        
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/**").hasAnyRole("ASESOR_COMERCIAL", "SUPERVISOR_INVENTARIO", "OPERARIO_PICKING", "OPERARIO_DESPACHO")
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ASESOR_COMERCIAL", "SUPERVISOR_INVENTARIO")
                        
                        .requestMatchers("/api/v1/picking/**").hasAnyRole("OPERARIO_PICKING", "SUPERVISOR_INVENTARIO")
                        .requestMatchers("/api/v1/despacho/**").hasAnyRole("OPERARIO_DESPACHO", "SUPERVISOR_INVENTARIO")
                        .requestMatchers("/api/v1/recepciones/**", "/api/v1/recepcion/**").hasAnyRole("SUPERVISOR_INVENTARIO", "OPERARIO_RECEPCION")
                        .requestMatchers(HttpMethod.POST, "/api/v1/manifiestos/**", "/api/v1/manifiesto/**").hasRole("SUPERVISOR_INVENTARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/manifiestos/**", "/api/v1/manifiesto/**").hasRole("SUPERVISOR_INVENTARIO")
                        .requestMatchers("/api/v1/manifiestos/**", "/api/v1/manifiesto/**").hasAnyRole("SUPERVISOR_INVENTARIO", "ASESOR_COMERCIAL", "OPERARIO_RECEPCION")
                        .requestMatchers("/api/v1/inventario/**").hasAnyRole("SUPERVISOR_INVENTARIO", "ASESOR_COMERCIAL")
                        .requestMatchers("/api/v1/excepciones/**", "/api/v1/excepcion/**").hasAnyRole("SUPERVISOR_INVENTARIO", "OPERARIO_PICKING", "OPERARIO_DESPACHO", "OPERARIO_RECEPCION")
                        .requestMatchers("/api/v1/operarios/**").hasRole("SUPERVISOR_INVENTARIO")
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}