package com.teakter.likes.security;

import com.teakter.likes.dto.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @DisplayName("debe pasar el filtro correctamente con los hencabezados correctos")
    void doFilter_WithValidHeaders_ShouldSetAuthentication() throws ServletException, IOException{
    when(request.getHeader("X-User-Id")).thenReturn("123");
    when(request.getHeader("X-User-Role")).thenReturn("USER");

    filter.doFilterInternal(request, response, filterChain );

    var auth=SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);

    UserPrincipal principal=(UserPrincipal) auth.getPrincipal();
    assertEquals(123L, principal.userId());
assertTrue(auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

verify(filterChain).doFilter(request, response);
}

@Test
    @DisplayName("debe resultar con el contexto vacío por el hencabezado nulo")
    void doFilter_WithoutHeaders_ShouldNotSetAuthentication() throws ServletException, IOException{
    when(request.getHeader("X-User-Id")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);


    var auth=SecurityContextHolder.getContext().getAuthentication();
    assertNull(auth);

    verify(filterChain).doFilter(request, response);
}

@Test
    @DisplayName("debe resultar con el contexto vacío por el formato invalido de el hencabezado")
    void doFilter_WithInvalidHeaders_ShouldNotSetAuthentication() throws ServletException, IOException{
    when(request.getHeader("X-User-Id")).thenReturn("abc");

    filter.doFilterInternal(request, response, filterChain);

    var auth=SecurityContextHolder.getContext().getAuthentication();
    assertNull(auth);

    verify(filterChain).doFilter(request, response);
}

}
