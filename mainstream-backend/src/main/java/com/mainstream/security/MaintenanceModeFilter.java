package com.mainstream.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainstream.settings.service.AppSettingsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final AppSettingsService settingsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Check if maintenance mode is enabled
        boolean maintenanceModeEnabled = settingsService.isMaintenanceModeEnabled();

        if (maintenanceModeEnabled) {
            log.debug("Maintenance mode is enabled - checking user role");

            // Check if user is authenticated and is an admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = false;

            if (authentication != null && authentication.isAuthenticated()) {
                isAdmin = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ROLE_ADMIN"));
            }

            if (!isAdmin) {
                log.info("Blocking non-admin request during maintenance mode: {} {}",
                         request.getMethod(), request.getRequestURI());

                // Return 503 Service Unavailable
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json");

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Service Unavailable");
                errorResponse.put("message", "The application is currently in maintenance mode. Please try again later.");
                errorResponse.put("status", 503);

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }

            log.debug("Admin user allowed during maintenance mode");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Don't apply maintenance mode to these paths
        boolean shouldSkip = path.startsWith("/api/auth/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.equals("/api/settings/maintenance-mode");  // Allow checking maintenance mode status

        log.debug("Maintenance Filter shouldNotFilter for path {}: {}", path, shouldSkip);
        return shouldSkip;
    }
}
