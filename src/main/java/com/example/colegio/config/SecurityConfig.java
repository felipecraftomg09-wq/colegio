package com.example.colegio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // codificar contraseñas de manera segura
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración del filtro de seguridad de Spring Security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http   // Permite el acceso a todas las rutas sin autenticación, desactivando la protección
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
              
                .requestMatchers("/**").permitAll()
            )
            .headers(headers -> 
                headers.frameOptions(frameOptions -> frameOptions.disable())
            );
        return http.build();
    }
}
