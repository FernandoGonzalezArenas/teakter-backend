package com.teakter.auth.service;

import com.teakter.auth.config.RabbitMQConfig;
import com.teakter.auth.dto.*;
import com.teakter.auth.entity.UserEntity;
import com.teakter.auth.entity.VerificationTokenEntity;
import com.teakter.auth.repository.RefreshTokenRepository;
import com.teakter.auth.repository.UserRepository;
import com.teakter.auth.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
private final AuthenticationManager authenticationManager;
private final RefreshTokenService refreshTokenService;
private final RabbitTemplate rabbitTemplate;

@Override
@Transactional
public void register(RegisterRequest request){
    if (userRepository.findByUsername(request.getUsername()).isPresent()){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "el usuario ya existe");
    }

    UserEntity user=new UserEntity();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmail(request.getEmail());
    user.setAddress(request.getAddress());
    user.setPhone(request.getPhone());
    user.setIsVerified(false);
    userRepository.save(user);

    //crear token de verificacion de cuenta
    String token= UUID.randomUUID().toString();
    VerificationTokenEntity verificationToken=VerificationTokenEntity.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(24))
            .build();
    verificationTokenRepository.save(verificationToken);

    Long userId= user.getId();
    String publicUserId= user.getPublicUserId();
    String username=user.getUsername();
UserRabbitDto message=new UserRabbitDto(userId, publicUserId, username);
rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_CREATE_RK, message);

//enviar evento de notificacion para verificacion a el email
    EmailVerificationRabbitDto emailMessage= new EmailVerificationRabbitDto(
            user.getEmail(),
            username,
            token);
    rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, "user.email.verify.rk", emailMessage);
}

@Override
public AuthResponse login(LoginRequest request){
    UserEntity user=userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario o contraseña incorrectos"));

    //verificar si la cuenta esta verificada con el email antes de autenticar
    if (Boolean.FALSE.equals(user.getIsVerified())){
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "debes verificar tu correo electronico para iniciar sesion");
    }

try {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    //se crean los tokens
AuthResponse newTokens=refreshTokenService.generateTokensForUser(request.getUsername());

//se registra el refresh token en la base de datos
refreshTokenService.registerRefresh(newTokens.getRefreshToken(), request.getUsername());
    System.out.println("refreshToken registrado en la base de datos");

//se retornan los nuevos tokens
    return newTokens;
}catch (BadCredentialsException e){
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario o contraseña incorrectos");
}catch (Exception e){
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error en la autenticacion");
}
}

@Override
@Transactional
public void verifyAccount(String token){
    VerificationTokenEntity verificationToken=verificationTokenRepository.findByToken(token)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "token invalido o inexistente"));

if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
    verificationTokenRepository.delete(verificationToken);
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "el token de verificacion ha expirado");
}

UserEntity user=verificationToken.getUser();
user.setIsVerified(true);
userRepository.save(user);

//borrar el token usado
    verificationTokenRepository.delete(verificationToken);
}

@Override
@Transactional
    public void banUser(String userId){
    UserEntity user=userRepository.findByPublicUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "cuenta de usuario no encontrada"));

    refreshTokenRepository.deleteByUserId(user.getId());
userRepository.deleteById(user.getId());
}

@Override
    public DataJwtUser getUserById(String userId){
UserEntity user=userRepository.findByPublicUserId(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

return DataJwtUser.builder()
        .username(user.getUsername())
        .userId(user.getPublicUserId())
        .role(user.getRole())
        .email(user.getEmail())
        .build();
}

}
