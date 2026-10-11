package com.teakter.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teakter.auth.dto.*;
import com.teakter.auth.security.CookieUtil;
import com.teakter.auth.security.JwtUtil;
import com.teakter.auth.service.AuthService;
import com.teakter.auth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class) //carga solo la capa MVC de el controlador
@AutoConfigureMockMvc(addFilters=false) //se desactivan los filtros de seguridad para esta prueba
public class AuthControllerTest {

@Autowired MockMvc mvc; //mock mvc para peticiones simuladas
@Autowired ObjectMapper mapper; //mapper para la serializacion Json

//mockear dependencias de el controlador
@MockBean
private AuthService authService;
@MockBean
private RefreshTokenService refreshTokenService;
@MockBean
private JwtUtil jwtUtil;
@MockBean
private UserDetailsService userDetailsService;
@MockBean
private CookieUtil cookieUtil;

private Long userId = 1L;

@BeforeEach
void  setup(){
    //configuracion por defecto de cookies para evitar el error NullPointerException
    ResponseCookie dummyCookie=ResponseCookie.from("token", "dummy_val").build();
    when(cookieUtil.createAccessTokenCookie(anyString())).thenReturn(dummyCookie);
    when(cookieUtil.createRefreshTokenCookie(anyString())).thenReturn(dummyCookie);
    when(cookieUtil.deleteAccessTokenCookie()).thenReturn(dummyCookie);
    when(cookieUtil.deleteRefreshTokenCookie()).thenReturn(dummyCookie);

}

@AfterEach
void tearDown(){
    SecurityContextHolder.clearContext(); //limpiesa de el contexto despues de cada prueba
}

@Test
void  register_201() throws  Exception{
//no retorna nada, solo verificamos el registro 201
    doNothing().when(authService).register(any(RegisterRequest.class));

    RegisterRequest req=new RegisterRequest();
    req.setUsername("fernando");
    req.setPassword("12345678");
    req.setEmail("fer@mail.com");

mvc.perform(post("/auth/register")
.contentType(MediaType.APPLICATION_JSON)
.content(mapper.writeValueAsString(req)))
.andExpect(status().isCreated())
.andExpect(content().string("usuario creado correctamente"));
    }

@Test
void  login_200_devuelveTokens() throws  Exception{
when(authService.login(any(LoginRequest.class)))
.thenReturn(new AuthResponse("ACC", "REF"));

LoginRequest req=new LoginRequest();
req.setUsername("fernando");
req.setPassword("12345678");

mvc.perform(post("/auth/login")
.contentType(MediaType.APPLICATION_JSON)
.content(mapper.writeValueAsString(req)))
.andExpect(status().isOk())
        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
        .andExpect(content().string("login exitoso"));
}

@Test
void  refresh_200_devuelveTokens() throws  Exception{
when(refreshTokenService.refreshToken("REF_TOKEN_VALIDO")).thenReturn(new AuthResponse("NEW_ACC", "NEW_REF"));

mvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", "REF_TOKEN_VALIDO")))
        .andExpect(status().isOk())
        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
        .andExpect(content().string("token renovado exitosamente"));
}

@Test
@DisplayName("debe fallar (401) si no se proporciona el refreshToken")
void refresh_sinCookie_401() throws Exception{
    //al no enviar cookie, el controlador retorna 401 UNAUTHORIZED
    mvc.perform(post("/auth/refresh"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("no se proporciono el refreshToken"));
}

@Test
void  logout_200_ok() throws  Exception{
doNothing().when(refreshTokenService).deleteByToken(anyString());

mvc.perform(post("/auth/logout")
                .cookie(new Cookie("refreshToken", "REF_TOKEN_VALIDO")))
        .andExpect(status().isOk())
        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
.andExpect(content().string("sesion cerrada correctamente"));
}

@Test
void  register_datosInvalidos_400() throws  Exception{
    RegisterRequest req=new RegisterRequest();
    req.setUsername("yo");
    req.setPassword("123");
    req.setEmail("mi correo");

    mvc.perform(post("/auth/register")
.contentType(MediaType.APPLICATION_JSON)
.content(mapper.writeValueAsString(req)))
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.fieldErrors.username").exists())
.andExpect(jsonPath("$.fieldErrors.email").exists());
}

@Test
    void me_200_usuarioAutenticado() throws Exception{
    UserPrincipal principal=new UserPrincipal(userId, "user-auth");
    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

    DataJwtUser jwtUser=DataJwtUser.builder()
            .username("fernando")
            .userId(userId)
            .role("ROLE_USER")
            .email("fer@mail.com")
            .build();
    when(authService.getUserById(userId)).thenReturn(jwtUser);

    mvc.perform(get("/auth/me")
                    .with(request -> {
                        request.setUserPrincipal(auth);
                        return request;
                    }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("fernando"))
            .andExpect(jsonPath("$.email").value("fer@mail.com"));
}

}
