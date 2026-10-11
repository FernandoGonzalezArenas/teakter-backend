package com.teakter.admin.security;

import com.teakter.admin.dto.UserPrincipal;
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
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;

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
    void doFilter_WithValidHeaders_ShouldSetAuthentication() throws ServletException, IOException{
when(request.getHeader("X-User-Id")).thenReturn("123");
when(request.getHeader("X-User-Role")).thenReturn("ADMIN");

filter.doFilterInternal(request, response, filterChain);

var auth= SecurityContextHolder.getContext().getAuthentication();
assertNotNull(auth);

    UserPrincipal principal=(UserPrincipal) auth.getPrincipal();
    assertEquals(123L, principal.userId());
    assertTrue(auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

    verify(filterChain).doFilter(request, response);
}

@Test
    void doFilter_WithoutHeaders_ShouldNotSetAuthentication() throws ServletException, IOException{
    when(request.getHeader("X-User-Id")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    var auth=SecurityContextHolder.getContext().getAuthentication();
    assertNull(auth);
    verify(filterChain, times(1)).doFilter(request, response);
}

@Test
void doFilter_WithInvalidFormat_ShouldNotSetAuthentication() throws ServletException, IOException{
    when(request.getHeader("X-User-Id")).thenReturn("abc");

    filter.doFilterInternal(request, response, filterChain);

    var auth=SecurityContextHolder.getContext().getAuthentication();

    assertNull(auth);
    verify(filterChain, times(1)).doFilter(request, response);
}

}
