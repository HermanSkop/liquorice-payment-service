package org.example.liquoricepaymentservice.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        log.debug("Converting JWT to Authentication token");
        List<String> roles = jwt.getClaimAsStringList("roles");
        log.debug("Found roles in JWT: {}", roles);

        Collection<GrantedAuthority> authorities;
        if (roles != null && !roles.isEmpty()) {
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        } else {
            authorities = Collections.emptyList();
        }

        log.debug("Created authorities: {}", authorities);
        return new JwtAuthenticationToken(jwt, authorities);
    }
}