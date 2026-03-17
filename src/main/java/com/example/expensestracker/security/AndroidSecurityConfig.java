package com.example.expensestracker.security;

import com.example.expensestracker.config.RateLimitFilter;
import com.example.expensestracker.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@RequiredArgsConstructor
public class AndroidSecurityConfig {
        private final JwtTokenFilter jwtTokenFilter;
        private final RateLimitFilter rateLimitFilter;
        @Value("${prefix}")
        private String apiPrefix;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(rateLimitFilter, JwtTokenFilter.class)
                                .authorizeHttpRequests(requests -> {
                                        requests
                                                        .requestMatchers(
                                                                        String.format("%s/users/register", apiPrefix),
                                                                        String.format("%s/users/login", apiPrefix),
                                                                        String.format("%s/users/refresh-token",
                                                                                        apiPrefix),
                                                                        String.format("%s/users/send-otp", apiPrefix),
                                                                        String.format("%s/users/verify-otp", apiPrefix),
                                                                        String.format("%s/users/reset-password",
                                                                                        apiPrefix),
                                                                        String.format("%s/internal/**", apiPrefix))
                                                        .permitAll()
                                                        // Require JWT for forecast endpoints that mobile app hits
                                                        .requestMatchers(String.format("%s/forecast/**", apiPrefix))
                                                        .authenticated()
                                                        .anyRequest().authenticated();
                                })
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .contentTypeOptions(contentType -> {
                                                })
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000))
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; script-src 'self'; object-src 'none';")));
                return http.build();
        }
}
