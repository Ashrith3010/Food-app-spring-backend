package com.food_api.food_api.service.jwt;

import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                if (jwtService.validateToken(jwt)) {
                    Claims claims = jwtService.extractClaims(jwt);
                    String username = claims.get("username", String.class);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                        // Create authentication with proper authorities
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()  // This should include ROLE_NGO or ROLE_DONOR
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Add debug logging
                        System.out.println("User authorities: " + user.getAuthorities());
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT Authentication error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        filterChain.doFilter(request, response);
    }
}