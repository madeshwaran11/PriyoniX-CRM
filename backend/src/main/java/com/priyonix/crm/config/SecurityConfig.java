package com.priyonix.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =========================================================
    // CORS
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
    List.of(
        "http://localhost:5173",
        "https://priyonix-crm.vercel.app"
    )
);
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------

                .csrf(csrf ->
                        csrf.disable()
                )

                // -------------------------------------------------
                // CORS
                // -------------------------------------------------

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // -------------------------------------------------
                // AUTHORIZATION
                // -------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // PUBLIC ENDPOINTS
                        // =========================================

                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/password/forgot",
                                "/api/password/reset"
                        ).permitAll()

                        // =========================================
                        // CURRENT USER
                        // =========================================

                        .requestMatchers(
                                "/api/users/me"
                        ).authenticated()

                        // =========================================
                        // ACTIVE USERS
                        // Used for allowed assignment dropdowns
                        // =========================================

                        .requestMatchers(
                                "/api/users/active"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // USER MANAGEMENT
                        // ADMIN ONLY
                        // =========================================

                        .requestMatchers(
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        // =========================================
                        // REPORTS
                        // ADMIN + MANAGER
                        // =========================================

                        .requestMatchers(
                                "/api/reports/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        // =========================================
                        // TASKS - VIEW
                        // ALL ROLES
                        // Service layer filters ownership
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/tasks/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // TASKS - CREATE
                        // ALL ROLES
                        // Service layer controls assignment
                        // =========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/tasks/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // TASKS - UPDATE
                        // ALL ROLES
                        // Service layer controls ownership
                        // =========================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/tasks/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // TASKS - DELETE
                        // ADMIN + MANAGER
                        // =========================================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/tasks/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        // =========================================
                        // LEADS
                        // ALL ROLES can reach the API
                        // Service layer controls permissions
                        // =========================================

                        .requestMatchers(
                                "/api/leads/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // CUSTOMERS
                        // ALL ROLES can reach the API
                        // Service layer controls permissions
                        // =========================================

                        .requestMatchers(
                                "/api/customers/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // FOLLOW-UPS
                        // ALL ROLES can reach the API
                        // Service layer controls permissions
                        // =========================================

                        .requestMatchers(
                                "/api/follow-ups/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // ACTIVITIES
                        // ALL ROLES can reach the API
                        // Service layer controls permissions
                        // =========================================

                        .requestMatchers(
                                "/api/activities/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "SALES",
                                "EMPLOYEE"
                        )

                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}