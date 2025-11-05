package com.mainstream.security;

import com.mainstream.user.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.debug("JWT Filter processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            log.debug("Extracted JWT token: {}", jwt != null ? "present" : "not found");
            
            if (StringUtils.hasText(jwt)) {
                log.debug("Validating JWT token");
                if (jwtUtil.validateToken(jwt)) {
                    log.debug("JWT token is valid");
                } else {
                    log.debug("JWT token validation failed");
                }
            }
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                String role = jwtUtil.getRoleFromToken(jwt);

                // Create wrapped request with custom headers
                JwtAuthenticationHttpServletRequestWrapper wrappedRequest = 
                    new JwtAuthenticationHttpServletRequestWrapper(request);
                wrappedRequest.putHeader("X-User-Id", userId.toString());
                wrappedRequest.putHeader("X-User-Role", role);
                wrappedRequest.putHeader("X-User-Email", email);

                // Set Spring Security context
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Successfully authenticated user: {} with role: {}", email, role);
                
                filterChain.doFilter(wrappedRequest, response);
                return;
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/api/auth/") || 
               path.startsWith("/actuator/health") || 
               path.startsWith("/actuator/info");
        
        log.debug("JWT Filter shouldNotFilter for path {}: {}", path, shouldSkip);
        return shouldSkip;
    }
}