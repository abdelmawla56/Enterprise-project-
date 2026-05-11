package edu.csai.youssef_service.security;

import edu.csai.youssef_service.multitenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ── Correlation ID for distributed tracing ──────────────────────────
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-Id", correlationId);

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtils.validateToken(token)) {
                    String email        = jwtUtils.getEmailFromToken(token);
                    Long   tenantId     = jwtUtils.getTenantIdFromToken(token);
                    String rolesString  = jwtUtils.getRolesFromToken(token);

                    // Set tenant scope for this request thread
                    TenantContext.setCurrentTenant(tenantId);

                    // Build authorities from roles claim (comma-separated)
                    List<SimpleGrantedAuthority> authorities = (rolesString != null && !rolesString.isBlank())
                            ? Arrays.stream(rolesString.split(","))
                                    .map(String::trim)
                                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                    .collect(Collectors.toList())
                            : List.of();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    MDC.put("tenantId", String.valueOf(tenantId));
                    MDC.put("userEmail", email);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }
}

