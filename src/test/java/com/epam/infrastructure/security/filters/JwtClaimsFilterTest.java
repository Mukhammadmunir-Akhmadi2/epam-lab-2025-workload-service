package com.epam.infrastructure.security.filters;

import com.epam.infrastructure.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtClaimsFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtClaimsFilter filter = new JwtClaimsFilter(jwtService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSetSecurityContext_whenValidBearerToken_andNoExistingAuth() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.isTokenValid("good")).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims("good")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("john");

        when(claims.get("auth")).thenReturn(Arrays.asList("SYSTEM", null, "ADMIN"));

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("john", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
        assertEquals(2, auth.getAuthorities().size());

        verify(chain).doFilter(req, res);
        verify(jwtService).isTokenValid("good");
        verify(jwtService).extractAllClaims("good");
    }

    @Test
    void doFilterInternal_shouldNotSetSecurityContext_whenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldNotSetSecurityContext_whenTokenInvalid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.isTokenValid("bad")).thenReturn(false);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
        verify(jwtService).isTokenValid("bad");
        verify(jwtService, never()).extractAllClaims(anyString());
    }

    @Test
    void doFilterInternal_shouldNotOverrideExistingAuthentication() throws Exception {
        var existing = new UsernamePasswordAuthenticationToken("existing", "n/a", List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.isTokenValid("good")).thenReturn(true);

        filter.doFilter(req, res, chain);

        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());

        verify(chain).doFilter(req, res);
        verify(jwtService).isTokenValid("good");
        verify(jwtService, never()).extractAllClaims(anyString());
    }

    @Test
    void doFilterInternal_shouldSetEmptyAuthorities_whenAuthClaimMissingOrNotList() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.isTokenValid("good")).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(jwtService.extractAllClaims("good")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("john");
        when(claims.get("auth")).thenReturn("SYSTEM");

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("john", auth.getPrincipal());
        assertTrue(auth.getAuthorities().isEmpty());

        verify(chain).doFilter(req, res);
        verify(jwtService).isTokenValid("good");
        verify(jwtService).extractAllClaims("good");
    }
}