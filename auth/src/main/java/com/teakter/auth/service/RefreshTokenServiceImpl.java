package com.teakter.auth.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.teakter.auth.dto.AuthResponse;
import com.teakter.auth.entity.RefreshTokenEntity;
import com.teakter.auth.repository.RefreshTokenRepository;
import com.teakter.auth.security.CustomUserDetails;
import com.teakter.auth.security.JwtUtil;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService{

private final RefreshTokenRepository refreshTokenRepository;
private final JwtUtil jwtUtil;
private final UserDetailsService userDetailsService;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil, @Qualifier("customUserDetailsService") UserDetailsService userDetailsService){
    this.refreshTokenRepository=refreshTokenRepository;
    this.jwtUtil=jwtUtil;
    this.userDetailsService=userDetailsService;
}

@Override
    public Optional<RefreshTokenEntity> findByToken(String token){
    return refreshTokenRepository.findByToken(token);
}

@Override
public AuthResponse generateTokensForUser(String username){
    CustomUserDetails userDetails=userDetailsMethod(username);

String role=userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .findFirst().orElse("ROLE_USER");

    String access= jwtUtil.generateToken(userDetails.getUsername(), userDetails.getPublicUserId(), role, userDetails.getEmail());
    String refresh=jwtUtil.generateRefreshToken(userDetails.getUsername(), userDetails.getPublicUserId(), role, userDetails.getEmail());
    return new AuthResponse(access, refresh);
}

@Override
public void registerRefresh(String refresh, String username){
CustomUserDetails userDetails=userDetailsMethod(username);
    RefreshTokenEntity refreshTokenEntity=new RefreshTokenEntity();
    refreshTokenEntity.setToken(refresh);
    refreshTokenEntity.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshExpirationTime()));
    refreshTokenEntity.setUser(userDetails.getUserEntity());
    refreshTokenRepository.save(refreshTokenEntity);
}

    @Override
    public AuthResponse refreshToken(String refreshToken){
        if (refreshToken==null || refreshToken.isBlank()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "se requiere un refreshToken valido");
        }

        RefreshTokenEntity refresh=refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refreshToken no encontrado o expirado"));

        if (refresh.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refresh);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refreshToken expirado");
        }

        String username=jwtUtil.extractUsername(refreshToken);
        if (username==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token invalido");
        }

        CustomUserDetails userDetails=userDetailsMethod(username);
        if (jwtUtil.isTokenValid(refreshToken, userDetails.getUsername())){
            //eliminando el refresh token para remplasarlo por uno nuevo
            refreshTokenRepository.delete(refresh);

            //generando nuevos tokens de acceso y refresco
AuthResponse newTokens=generateTokensForUser(username);

            //actualizando la informacion en la base de datos con la de el nuevo refreshToken generado
registerRefresh(newTokens.getRefreshToken(), userDetails.getUsername());

//retornando los nuevos tokens
            return newTokens;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token invalido");
    }

@Override
    public void deleteByToken(String token){
    refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
}

private CustomUserDetails userDetailsMethod(String username){
    CustomUserDetails userDetails=(CustomUserDetails) userDetailsService.loadUserByUsername(username);
    return userDetails;
}

}
