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
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Login details are checked against the users table.
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
                                "/users/**"
                        ).hasRole("MANAGER")

                        .requestMatchers("/employee-dashboard")
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                // Shows a friendly page instead of the default Whitelabel page.
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