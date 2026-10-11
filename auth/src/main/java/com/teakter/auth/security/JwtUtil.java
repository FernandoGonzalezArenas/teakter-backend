package com.teakter.auth.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

@Value("${jwt.secret}")
    private String secretKey;

@Value("${jwt.expiration}")
    private long expirationTime;

@Value("${jwt.refreshExpiration}")
    private long refreshExpirationTime;

public String generateToken(String username, String userId, String role, String email){
    return createToken(username, userId, role, email, expirationTime);
}

public String generateRefreshToken(String username, String userId, String role, String email){
    return createToken(username, userId, role, email, refreshExpirationTime);
}

private String createToken(String username, String userId, String role, String email, long expiration){
return JWT.create()
        .withSubject(username)
        .withClaim("userId", userId)
        .withClaim("role", role)
        .withClaim("email", email)
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
        .sign(Algorithm.HMAC256(secretKey));
}

public String extractUsername(String token){
    try {
        DecodedJWT decodedJWT=verifyToken(token);
        return decodedJWT.getSubject();
    }catch (JWTVerificationException e){
return null;
    }
}

public boolean isTokenValid(String token, String username){
    try {
        DecodedJWT decodedJWT=verifyToken(token);
        return decodedJWT.getSubject().equals(username);
    }catch (JWTVerificationException e){
return false;
    }
}

private DecodedJWT verifyToken(String token){
    JWTVerifier verifier=JWT.require(Algorithm.HMAC256(secretKey)).build();
    return verifier.verify(token);
}

public String extractClaim(String token, String nameClaim){
    try {
        DecodedJWT decodedJWT=verifyToken(token);
        return decodedJWT.getClaim(nameClaim).asString();
    } catch (JWTVerificationException e){
        return null;
    }
}

public long getRefreshExpirationTime(){
    return refreshExpirationTime;
}

}
