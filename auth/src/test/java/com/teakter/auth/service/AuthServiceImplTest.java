package com.teakter.auth.service;

import com.teakter.auth.dto.AuthResponse;
import com.teakter.auth.dto.LoginRequest;
import com.teakter.auth.dto.RegisterRequest;
import com.teakter.auth.entity.UserEntity;
import com.teakter.auth.repository.RefreshTokenRepository;
import com.teakter.auth.repository.UserRepository;
import com.teakter.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceImplTest {

private UserRepository userRepository;
private PasswordEncoder passwordEncoder;
private AuthenticationManager authenticationManager;
private RefreshTokenService refreshTokenService;
private RefreshTokenRepository refreshTokenRepository;
private VerificationTokenRepository verificationTokenRepository;
private RabbitTemplate rabbitTemplate;

private AuthServiceImpl service;

@BeforeEach
void setUp(){
    userRepository=mock(UserRepository.class);
    refreshTokenRepository=mock(RefreshTokenRepository.class);
    verificationTokenRepository = mock(VerificationTokenRepository.class);
    passwordEncoder=mock(PasswordEncoder.class);
    authenticationManager=mock(AuthenticationManager.class);
    refreshTokenService=mock(RefreshTokenService.class);
    rabbitTemplate=mock(RabbitTemplate.class);
    service=new AuthServiceImpl(userRepository, refreshTokenRepository, verificationTokenRepository, passwordEncoder, authenticationManager, refreshTokenService, rabbitTemplate);
}

@Test
void register_usuarioYaExiste_400(){
    //el repositorio indica que el usuario ya existe
    when(userRepository.findByUsername("fernando")).thenReturn(Optional.of(new UserEntity()));

    //armamos el request
    RegisterRequest req=new RegisterRequest();
    req.setUsername("fernando");

    //esperamos codigo de estado 400
    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.register(req));

    //comprobamos el error de la respuesta
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
}

@Test
void register_OK_guardaConPasswordCodificado(){
    //no existe el usuario
    when(userRepository.findByUsername("mario")).thenReturn(Optional.empty());

    //encoder retorna ENC
    when(passwordEncoder.encode("12345678")).thenReturn("ENC");

    //creamos el objeto register
    RegisterRequest req=new RegisterRequest();
   req.setUsername("mario");
   req.setPassword("12345678");
   req.setEmail("mario@mail.com");
   service.register(req);
   
   //capturamos el UserEntity guardado
   ArgumentCaptor<UserEntity> cap=ArgumentCaptor.forClass(UserEntity.class);
   verify(userRepository).save(cap.capture());
   UserEntity saved=cap.getValue();
   assertEquals("mario", saved.getUsername());
   assertEquals("ENC", saved.getPassword());
   assertEquals("mario@mail.com", saved.getEmail());

   //verificamos que se llama a convertAndSent de RabbitMQ
    verify(rabbitTemplate, times(2)).convertAndSend(any(String.class), any(String.class), any(Object.class));
}

@Test
void login_OK_autentica_y_generaTokens(){

    //crear usuario en la base de datos para agregar el estado true de la verificacion
UserEntity mockUser=UserEntity.builder()
        .username("fernando")
        .password("secret98")
        .email("fer@mail.com")
        .isVerified(true)
        .build();
when(userRepository.findByUsername("fernando")).thenReturn(Optional.of(mockUser));

    //refreshTokenService devuelve tokens
    when(refreshTokenService.generateTokensForUser("fernando")).thenReturn(new AuthResponse("ACC", "REF"));

    //crear objeto LoginRequest con credenciales
    LoginRequest req=new LoginRequest();
    req.setUsername("fernando");
    req.setPassword("secret98");
    AuthResponse resp=service.login(req);

//comprobar que los tokens no son nulos
assertNotNull(resp.getAccessToken());
assertNotNull(resp.getRefreshToken());

//se devuelven los tokens generados
assertEquals("ACC", resp.getAccessToken());
assertEquals("REF", resp.getRefreshToken());

//verificamos que se registro el refreshToken
verify(refreshTokenService).registerRefresh("REF", "fernando");

//verificamos que el usuario se autentico
verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("fernando", "secret98"));
}

@Test
void login_credencialesInvalidas_401(){
    //simula credenciales invalidas
    doThrow(new BadCredentialsException("bad"))
    .when(authenticationManager).authenticate(any());

    //crear objeto LoginRequest con malas credenciales
    LoginRequest req=new LoginRequest();
    req.setUsername("fernando");
    req.setPassword("oops");

    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.login(req));
assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
}

@Test
void login_errorGenerico_500(){
UserEntity user=UserEntity.builder()
                .username("fernando")
                        .password("oops")
                                .email("fer@mail.com")
                                        .isVerified(true)
                                                .build();
when(userRepository.findByUsername("fernando")).thenReturn(Optional.of(user));
when(refreshTokenService.generateTokensForUser("fernando")).thenReturn(new AuthResponse("ACC", "REF"));

    //generar el error 500 de el servidor
    doThrow(new RuntimeException("Error inesperado en el servidor"))
            .when(authenticationManager).authenticate(any());

//crear objecto login para error 500
LoginRequest req=new LoginRequest();
req.setUsername("fernando");
req.setPassword("oops");

//comprobar el error arrojado
ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.login(req));
assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
}

@Test
    @DisplayName("debe borrar a el usuario y a sus tokens de la base de datos")
    void banUser_ShouldDeleteUserAndTokens(){
    assertDoesNotThrow(() -> service.banUser(1L));
    verify(refreshTokenRepository, times(1)).deleteByUserId(1L);
    verify(userRepository, times(1)).deleteById(1L);
    }


}
