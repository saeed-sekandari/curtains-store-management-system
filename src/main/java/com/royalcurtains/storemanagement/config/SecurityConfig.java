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

                        // Public pages.
                        .requestMatchers(
                                "/",
                                "/home",
                                "/login",
                                "/access-denied",
                                "/css/**"
                        ).permitAll()

                        // Manager-only pages.
                        .requestMatchers(
                                "/dashboard",
                                "/users/**"
                        ).hasRole("MANAGER")

                        // Tailor-only page.
                        .requestMatchers(
                                "/tailor-dashboard"
                        ).hasRole("TAILOR")

                        // Employees, accountants, and managers may use
                        // store operations and payment-related pages.
                        .requestMatchers(
                                "/customers/**",
                                "/orders/**",
                                "/payments/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "EMPLOYEE",
                                "ACCOUNTANT"
                        )

                        // Regular employee dashboard.
                        .requestMatchers(
                                "/employee-dashboard"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "ACCOUNTANT"
                        )

                        // Everything else requires login.
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}