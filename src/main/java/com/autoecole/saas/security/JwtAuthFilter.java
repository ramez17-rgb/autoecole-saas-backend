package com.autoecole.saas.security;

import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.equals("/api/students/register")
                || path.equals("/api/monitors/register")
                || path.equals("/api/tenants/register");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = authHeader.substring(7);
            String email = jwtService.extractUsername(jwt);
            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null && jwtService.isTokenValid(jwt, user)) {
                    if (user.getTenant() != null && !user.getTenant().isActive()) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    UUID tenantId = jwtService.extractTenantId(jwt);
                    TenantContext.setTenantId(tenantId);
                    request.setAttribute("tenantId", tenantId);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            jwtService.getAuthorities(user));
                    System.out.println("Authenticated User: " + user.getEmail());
                    System.out.println("Authorities: " + authToken.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("JWT Authentication Warning: " + e.getMessage());
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
        TenantContext.clear();

    }
}
