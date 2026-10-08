package com.royalcurtains.storemanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedLoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        boolean isManager = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MANAGER"));

        boolean isTailor = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_TAILOR"));

        if (isManager) {
            // Managers can view both stores and manage the system.
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "/dashboard");

        } else if (isTailor) {
            // Tailors only see their assigned measurements and work.
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "/tailor-dashboard");

        } else {
            // Employees and accountants use the regular employee dashboard.
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "/employee-dashboard");
        }
    }
}