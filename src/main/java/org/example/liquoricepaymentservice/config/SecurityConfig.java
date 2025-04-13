package org.example.liquoricepaymentservice.config;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http
                .securityMatcher(Constants.BASE_PATH + "/**")
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("CUSTOMER"))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.authenticationManager(authenticationManager)))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtDecoder jwtDecoder, JwtRoleConverter jwtRoleConverter) {
        JwtAuthenticationProvider authManager = new JwtAuthenticationProvider(jwtDecoder);
        authManager.setJwtAuthenticationConverter(jwtRoleConverter);
        return authManager::authenticate;
    }
}
