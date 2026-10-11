package com.teakter.gateway.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

private JwtUtil jwtUtil;
private final String SECRET="test_secret_key_012345678901234567890";

@BeforeEach
    void setup(){
    jwtUtil=new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET);
}

@Test
    void shouldExtractUserIdCorrectly(){
    String token= JWT.create()
        .withClaim("userId", "100")
        .sign(Algorithm.HMAC256(SECRET));

    String userId=jwtUtil.extractUserId(token);
assertEquals("100", userId);
}

@Test
    void shouldReturnTrueIfTokenIsExpired(){
    String token= JWT.create()
        .withExpiresAt(new Date(System.currentTimeMillis() -1000))
        .sign(Algorithm.HMAC256(SECRET));

    assertTrue(jwtUtil.isTokenExpired(token));
}

}
