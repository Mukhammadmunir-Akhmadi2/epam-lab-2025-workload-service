package com.epam.infrastructure.security;

import com.epam.application.exceptions.InvalidAuthException;
import com.epam.infrastructure.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticatorTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtAuthenticator authenticator = new JwtAuthenticator(jwtService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticate_shouldSetSecurityContext_whenTokenValidAndHasSystemAuthority() {
        String header = "Bearer token123";

        when(jwtService.isTokenValid("token123")).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims("token123")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("system-user");
        when(claims.get("auth")).thenReturn(List.of("SYSTEM", "OTHER"));

        authenticator.authenticate(header);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("system-user", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM")));
    }

    @Test
    void authenticate_shouldThrow_whenHeaderMissingOrNotBearer() {
        InvalidAuthException ex1 = assertThrows(InvalidAuthException.class,
                () -> authenticator.authenticate(null));
        assertTrue(ex1.getMessage().toLowerCase().contains("invalid"));

        InvalidAuthException ex2 = assertThrows(InvalidAuthException.class,
                () -> authenticator.authenticate("Basic abc"));
        assertTrue(ex2.getMessage().toLowerCase().contains("invalid"));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_shouldThrow_whenTokenInvalid() {
        when(jwtService.isTokenValid("bad")).thenReturn(false);

        InvalidAuthException ex = assertThrows(InvalidAuthException.class,
                () -> authenticator.authenticate("Bearer bad"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenValid("bad");
        verify(jwtService, never()).extractAllClaims(anyString());
    }

    @Test
    void authenticate_shouldThrow_whenSystemAuthorityMissing() {
        when(jwtService.isTokenValid("token123")).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims("token123")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user");
        when(claims.get("auth")).thenReturn(List.of("USER"));

        InvalidAuthException ex = assertThrows(InvalidAuthException.class,
                () -> authenticator.authenticate("Bearer token123"));
        assertTrue(ex.getMessage().contains("SYSTEM"));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}