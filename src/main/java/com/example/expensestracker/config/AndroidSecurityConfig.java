package com.example.expensestracker.config;

import com.example.expensestracker.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@RequiredArgsConstructor
public class AndroidSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers(
                                    String.format("/api/users/register"),
                                    String.format("/%s/users/login", apiPrefix),
                                    String.format("/%s/users/**", apiPrefix),
                                    String.format("/api/categories"),
                                    String.format("/api/categories/**"),
                                    String.format("/api/transactions"),
                                    String.format("/api/transactions/**"),
                                    String.format("/api/finance"),
                                    String.format("/api/**"),
                                    String.format("api/fixed-transactions/**"),
                                    String.format("api/fixed-transactions"),
                                    String.format("api/category-limits"),
                                    String.format("api/category-limits/**"),
                                    String.format("api/report"),
                                    String.format("api/report/**")
                            )
                            .permitAll()
                            .anyRequest().authenticated();
                });

        return http.build();
    }
}
