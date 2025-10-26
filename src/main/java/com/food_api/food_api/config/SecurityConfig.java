package com.food_api.food_api.config;

import com.food_api.food_api.service.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/**").permitAll()  // Allow all requests to /api/**
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/donations").permitAll()

                        // Profile endpoints
                        .requestMatchers(HttpMethod.GET, "/api/account/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/account/profile").authenticated()

                        .requestMatchers("/api/donations/user").hasAnyRole("NGO", "DONOR")  // Add this line
                        .requestMatchers("/api/donations/**").authenticated()
                        // Donation endpoints with authentication
                        .requestMatchers("/api/donations/user").authenticated()  // Make sure this matches your controller
                        .requestMatchers(HttpMethod.POST, "/api/donations").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/donations/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/donations/**").authenticated()
                        .requestMatchers("/api/donations/**").authenticated()

                        // NGO specific endpoints
                        .requestMatchers("/api/ngo/**").hasRole("NGO")

                        // Catch all other requests
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow frontend origin
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Headers"
        ));

        // Expose headers that frontend can read
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Headers"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // How long the CORS pre-flight response should be cached
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
