package com.teakter.gateway.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.teakter.gateway.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {

@Mock
    private JwtUtil jwtUtil;
@Mock
    private GatewayFilterChain filterChain;
private AuthenticationFilter filter;

@BeforeEach
    void setup(){
    filter=new AuthenticationFilter(jwtUtil);
lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
}

@Test
@DisplayName("debe permitir rutas de auth sin autenticacion")
    void shouldAllowAuthRoutes(){
    MockServerWebExchange exchange=MockServerWebExchange
        .from(MockServerHttpRequest.post("/auth/login").build());
    filter.filter(exchange, filterChain).block();

    //verificamos que se llamo a el siguiente filtro en la cadena
    verify(filterChain).filter(any());
}

@Test
    @DisplayName("debe permitir get publico sin token (status de like)")
    void shouldAllowPublicGetWithoutToken(){
    MockServerWebExchange exchange=MockServerWebExchange
        .from(MockServerHttpRequest.get("/like/1/status").build());

    filter.filter(exchange, filterChain).block();

    verify(filterChain).filter(any());
}

@Test
    @DisplayName("debe denegar recurso protegido si no hay token")
void shouldDenyProtectedResourceWithoutToken(){
MockServerWebExchange exchange=MockServerWebExchange
    .from(MockServerHttpRequest.post("/comentarios").build());
filter.filter(exchange, filterChain).block();

//verificacion de error de no autorizacion
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
}

@Test
    @DisplayName("debe permitir y mutar peticion si el token es valido")
    void shouldMutateRequestWhenTokenIsValid(){
    String token="valid-token";
    MockServerWebExchange exchange=MockServerWebExchange
        .from(MockServerHttpRequest.post("/comentarios")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " +token)
            .build());

when(jwtUtil.isTokenExpired(token)).thenReturn(false);
when(jwtUtil.extractUserId(token)).thenReturn("55");

filter.filter(exchange, filterChain).block();

//capturamos la peticion mutada para ver si lleva el userId
ArgumentCaptor<ServerWebExchange> captor=ArgumentCaptor.forClass(ServerWebExchange.class);
verify(filterChain).filter(captor.capture());
String headerId=captor.getValue().getRequest().getHeaders().getFirst("X-User-Id");
assertEquals("55", headerId);
}

@Test
    @DisplayName("debe retornar 401 si el token es invalido o mal formado")
    void shouldReturn401IfTokenIsMalformed(){
    String token="token-basura";
    MockServerWebExchange exchange=MockServerWebExchange
        .from(MockServerHttpRequest.get("/videos/upload-url")
            .header(HttpHeaders.AUTHORIZATION, "Bearer "+ token)
            .build());

    when(jwtUtil.isTokenExpired(token)).thenThrow(new JWTVerificationException("error"));
    filter.filter(exchange, filterChain).block();

    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
}

}
