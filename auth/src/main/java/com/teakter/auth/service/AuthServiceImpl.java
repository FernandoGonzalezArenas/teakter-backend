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
    System.out.println("ingresando a elmetodo de registro de el service en el microservicio auth");
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

    System.out.println("usuario registrado correctamente aun sin verificacion de correo");
    //crear token de verificacion de cuenta
    String token= UUID.randomUUID().toString();
    VerificationTokenEntity verificationToken=VerificationTokenEntity.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(24))
            .build();
    verificationTokenRepository.save(verificationToken);

    Long userId= user.getId();
    String username=user.getUsername();
UserRabbitDto message=new UserRabbitDto(userId, username);
rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_CREATE_RK, message);

//enviar evento de notificacion para verificacion a el email
    EmailVerificationRabbitDto emailMessage= new EmailVerificationRabbitDto(
            user.getEmail(),
            username,
            token);
    rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, "user.email.verify.rk", emailMessage);
    System.out.println("datos enviados a microservicio notification desde micro auth: \ncorreo del usuario: "+ user.getEmail()+"\nusername: "+ username+"\ntoken: "+token+"\n");
}

@Override
public AuthResponse login(LoginRequest request){
    System.out.println("ingresando a el metodo de login de service en el microservicio auth");
    UserEntity user=userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario o contraseña incorrectos"));

    //verificar si la cuenta esta verificada con el email antes de autenticar
    if (Boolean.FALSE.equals(user.getIsVerified())){
        System.out.println("correo sin verificar");
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "debes verificar tu correo electronico para iniciar sesion");
    }
    System.out.println("correo verificado correctamente");


try {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    System.out.println("usuario autenticado correctamente");
    //se crean los tokens
AuthResponse newTokens=refreshTokenService.generateTokensForUser(request.getUsername());
    System.out.println("tokens JWT creados");

//se registra el refresh token en la base de datos
refreshTokenService.registerRefresh(newTokens.getRefreshToken(), request.getUsername());
    System.out.println("refreshToken registrado en la base de datos");

//se retornan los nuevos tokens
    return newTokens;
}catch (BadCredentialsException e){
    System.out.println("dentro de el catch de el metodo login, credenciales incorrectas");
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario o contraseña incorrectos");
}catch (Exception e){
    System.out.println("dentro de el catch de el metodo login, error interno de el servidor");
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error en la autenticacion");
}
}

@Override
@Transactional
public void verifyAccount(String token){
    System.out.println("ingresando a el metodo de verificacion de correo de service en el microservicio auth");

VerificationTokenEntity verificationToken=verificationTokenRepository.findByToken(token)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "token invalido o inexistente"));

if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
    verificationTokenRepository.delete(verificationToken);
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "el token de verificacion ha expirado");
}

UserEntity user=verificationToken.getUser();
user.setIsVerified(true);
userRepository.save(user);
    System.out.println("correo verificado correctamente");

//borrar el token usado
    verificationTokenRepository.delete(verificationToken);
}

@Override
@Transactional
    public void banUser(Long userId){
    refreshTokenRepository.deleteByUserId(userId);
userRepository.deleteById(userId);
System.out.println("usuario con el id "+userId+ " baneado permanentemente");
}

@Override
    public DataJwtUser getUserById(Long userId){
    System.out.println("dentro de el metodo para obtener los datos de el usuario autenticado en el microservicio auth");
UserEntity user=userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario no encontrado"));
    System.out.println("usuario autenticado encontrado");

return DataJwtUser.builder()
        .username(user.getUsername())
        .userId(user.getId())
        .role(user.getRole())
        .email(user.getEmail())
        .build();
}

}
