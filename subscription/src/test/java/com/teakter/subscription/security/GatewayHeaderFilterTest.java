package com.teakter.subscription.security;

import com.teakter.subscription.dto.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GatewayHeaderFilterTest {

@InjectMocks
    private GatewayHeaderFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setup(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_WithValidIdHeader_ShouldSetAuthentication() throws ServletException, IOException{
        when(request.getHeader("X-User-Id")).thenReturn("123");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);

        UserPrincipal principal=(UserPrincipal) auth.getPrincipal();
        assertEquals(123L, principal.userId());

        //verificamos que se llame a filterChain para que la cadena continue
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_WithoutHeader_ShouldNotSetAuthentication() throws ServletException, IOException{
        when(request.getHeader("X-User-Id")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_WithInvalidFormat_ShouldNotSetAuthentication() throws ServletException, IOException{
        when(request.getHeader("X-User-Id")).thenReturn("abc");

        //ejecutamos el metodo y el try catch interno manejara el error
        filter.doFilterInternal(request, response, filterChain);

        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain, times(1)).doFilter(request, response);
    }

}
