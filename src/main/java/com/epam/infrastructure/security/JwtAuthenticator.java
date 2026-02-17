package com.epam.infrastructure.security;

import com.epam.application.exceptions.InvalidAuthException;
import com.epam.infrastructure.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticator {

    private final JwtService jwtService;

    public void authenticate(String bearerHeader) {
        String token = getBearerToken(bearerHeader);
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new InvalidAuthException("Invalid or missing JWT token");
        }

        Claims claims = jwtService.extractAllClaims(token);
        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

        boolean isSystem = authorities.stream().anyMatch(a -> a.getAuthority().equals("SYSTEM"));
        if (!isSystem) {
            throw new InvalidAuthException("SYSTEM authority required");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object raw = claims.get("auth");
        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return List.of();
    }

    private String getBearerToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}