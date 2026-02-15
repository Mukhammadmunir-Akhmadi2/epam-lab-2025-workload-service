package com.epam.infrastructure.security.filters;

import com.epam.infrastructure.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtClaimsFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getBearerToken(request.getHeader("Authorization"));

        if (token != null
                && jwtService.isTokenValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            Claims claims = jwtService.extractAllClaims(token);

            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
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

        // fallback: no authorities in token
        return List.of();
    }

    private String getBearerToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}
