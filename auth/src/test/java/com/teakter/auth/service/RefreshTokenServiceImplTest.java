package com.teakter.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.ResponseStatusException;

import com.teakter.auth.dto.AuthResponse;
import com.teakter.auth.entity.RefreshTokenEntity;
import com.teakter.auth.entity.UserEntity;
import com.teakter.auth.repository.RefreshTokenRepository;
import com.teakter.auth.security.CustomUserDetails;
import com.teakter.auth.security.JwtUtil;

public class RefreshTokenServiceImplTest {

private RefreshTokenRepository refreshTokenRepository;
private JwtUtil jwtUtil;
private UserDetailsService userDetailsService;
private RefreshTokenServiceImpl service;

@BeforeEach
void setUp(){
    refreshTokenRepository=mock(RefreshTokenRepository.class);
    jwtUtil=mock(JwtUtil.class);
    userDetailsService=mock(UserDetailsService.class);
    service=new RefreshTokenServiceImpl(refreshTokenRepository, jwtUtil, userDetailsService);
}

private CustomUserDetails buildDetails(){
UserEntity u=userEntity();
return new CustomUserDetails(u, List.of(() -> "ROLE_USER"));
}

@Test
void generateTokensForUser_OK(){
    //el UserDetailsService devuelve detalles
    when(userDetailsService.loadUserByUsername("fernando")).thenReturn(buildDetails());
    //el JwtUtil genera los tokens
    when(jwtUtil.generateToken("fernando", "1", "ROLE_USER", "fernando@mail.com")).thenReturn("ACC");
    when(jwtUtil.generateRefreshToken("fernando", "1", "ROLE_USER", "fernando@mail.com")).thenReturn("REF");

    AuthResponse r=service.generateTokensForUser("fernando");

    assertEquals("ACC", r.getAccessToken());
    assertEquals("REF", r.getRefreshToken());
}

@Test
void registerRefresh_guardaConExpYUsuario(){
when(userDetailsService.loadUserByUsername("fernando")).thenReturn(buildDetails());
when(jwtUtil.getRefreshExpirationTime()).thenReturn(60_000L);
service.registerRefresh("REF", "fernando");

//capturamos lo que se guarda
ArgumentCaptor<RefreshTokenEntity> cap=ArgumentCaptor.forClass(RefreshTokenEntity.class);
verify(refreshTokenRepository).save(cap.capture());
RefreshTokenEntity saved=cap.getValue();
assertEquals("REF", saved.getToken());
assertNotNull(saved.getExpiryDate());
assertNotNull(saved.getUser());
assertEquals("fernando", saved.getUser().getUsername());
}

@Test
void refreshToken_flujoValido_generaYRemplasa(){
    //token entrante
    String incoming ="REF_OLD";

    //username del token
when(jwtUtil.extractUsername(incoming)).thenReturn("fernando");

//existe en bd y no esta expirado 
when(jwtUtil.isTokenValid(incoming, "fernando")).thenReturn(true);
UserEntity ue=userEntity();
RefreshTokenEntity entity=RefreshTokenEntity.builder()
.token(incoming)
.expiryDate(Instant.now().plusSeconds(100))
.user(ue)
.build();
when(refreshTokenRepository.findByToken(incoming)).thenReturn(Optional.of(entity));

//user y validacion OK
when(userDetailsService.loadUserByUsername("fernando")).thenReturn(buildDetails());

//cuando se generen nuevos tokens
//utilizaremos un espia para interseptar generateTokensForUser y registerRefresh
RefreshTokenServiceImpl spy=spy(service);
doReturn(new AuthResponse("NEW_ACC", "NEW_REF")).when(spy).generateTokensForUser("fernando");
doNothing().when(spy).registerRefresh("NEW_REF", "fernando");

AuthResponse out=spy.refreshToken(incoming);
assertEquals("NEW_ACC", out.getAccessToken());
assertEquals("NEW_REF", out.getRefreshToken());

//verificar que se borro el token viejo
verify(refreshTokenRepository).delete(entity);
//verificar si se registra el nuevo token
verify(spy).registerRefresh("NEW_REF", "fernando");
}

@Test
void  refreshToken_blanco_401(){
    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.refreshToken(" "));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
}

@Test
void  refreshToken_usernameNull_401(){
when(jwtUtil.extractUsername("x")).thenReturn(null);
ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.refreshToken("x"));
assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
}

@Test
void refreshToken_noExisteEnBD_401(){
when(jwtUtil.extractUsername("x")).thenReturn("fernando");
when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.empty());
ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.refreshToken("x"));
assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
}

@Test
void  refreshToken_expirado_borraYLanza_401(){
when(jwtUtil.extractUsername("x")).thenReturn("fernando");

//se simula un token en la base de datos
UserEntity u=userEntity();
RefreshTokenEntity expired=RefreshTokenEntity.builder()
.token("x")
.expiryDate(Instant.now().minusSeconds(1))
.user(u)
.build();
//se stupea la busqueda del token para retornar el token expirado
when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(expired));

//se guarda la excepcion generada
ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.refreshToken("x"));

//se compara la excepcion resultante para verificar si coinside con la esperada
assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

//se verifica si se borro el token expirado
verify(refreshTokenRepository).delete(expired);
}

@Test
void  refreshToken_tokenInvalido_401(){
when(jwtUtil.extractUsername("x")).thenReturn("fernando");

UserEntity u=userEntity();
RefreshTokenEntity ok=RefreshTokenEntity.builder()
.token("x")
.expiryDate(Instant.now().plusSeconds(100))
.user(u)
.build();
when(refreshTokenRepository.findByToken("x")).thenReturn(Optional.of(ok));
when(userDetailsService.loadUserByUsername("fernando")).thenReturn(buildDetails());
when(jwtUtil.isTokenValid("x", "fernando")).thenReturn(false);
ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.refreshToken("x"));
assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
}

@Test
void  deleteByToken_siExiste_loBorra(){
UserEntity u=userEntity();
    RefreshTokenEntity e=RefreshTokenEntity.builder()
.token("z")
.expiryDate(Instant.now().plusSeconds(50))
.user(u)
.build();

    when(refreshTokenRepository.findByToken("z")).thenReturn(Optional.of(e));
    service.deleteByToken("z");
    verify(refreshTokenRepository).delete(e);
}

private UserEntity userEntity(){
    return UserEntity.builder()
    .id(1L)
.username("fernando")
.password("12345678")
.email("fernando@mail.com")
.build();
}

}
