package com.royalcurtains.storemanagement.config;

import com.royalcurtains.storemanagement.security.DatabaseUserDetailsService;
import com.royalcurtains.storemanagement.security.RoleBasedLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
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

                        // All supplier and supplier payment features are manager-only.
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