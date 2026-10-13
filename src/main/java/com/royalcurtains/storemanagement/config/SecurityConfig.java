package com.royalcurtains.storemanagement.config;

import com.royalcurtains.storemanagement.security.DatabaseUserDetailsService;
import com.royalcurtains.storemanagement.security.RoleBasedLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final DatabaseUserDetailsService userDetailsService;
    private final RoleBasedLoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(
            DatabaseUserDetailsService userDetailsService,
            RoleBasedLoginSuccessHandler loginSuccessHandler) {

        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
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

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> {
                })

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**"
                        )
                )

                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/home",
                                "/login",
                                "/access-denied",
                                "/css/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/expenses/**"
                        ).hasRole("MANAGER")

                        .requestMatchers(
                                "/api/inventory/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "EMPLOYEE",
                                "ACCOUNTANT",
                                "TAILOR"
                        )

                        .requestMatchers(
                                "/dashboard",
                                "/dashboard/**",
                                "/users/**"
                        ).hasRole("MANAGER")

                        .requestMatchers(
                                "/tailor-dashboard",
                                "/tailor/work/**"
                        ).hasRole("TAILOR")

                        .requestMatchers(
                                "/employee-dashboard",
                                "/notifications/**"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "ACCOUNTANT"
                        )

                        .requestMatchers(
                                "/suppliers/**",
                                "/supplier-purchases/**",
                                "/supplier-payments/**"
                        ).hasRole("MANAGER")

                        .requestMatchers(
                                "/orders/*/items"
                        ).hasAnyRole(
                                "MANAGER",
                                "EMPLOYEE",
                                "TAILOR",
                                "ACCOUNTANT"
                        )

                        .requestMatchers(
                                "/orders/*/items/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "EMPLOYEE",
                                "ACCOUNTANT"
                        )

                        .requestMatchers(
                                "/customers/**",
                                "/orders/**",
                                "/payments/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "EMPLOYEE",
                                "ACCOUNTANT"
                        )

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}