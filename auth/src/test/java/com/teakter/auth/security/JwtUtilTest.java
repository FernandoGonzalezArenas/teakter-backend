package com.teakter.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtUtilTest {

private  JwtUtil jwtUtil;

@BeforeEach
void  setup(){
jwtUtil=new  JwtUtil(); //instanciamos la clase concreta sin spring

//seteamos los value privados con ReflectionTestUtils
ReflectionTestUtils.setField(jwtUtil, "secretKey", "ahfuerhdj");
ReflectionTestUtils.setField(jwtUtil, "expirationTime", 60_000L);
ReflectionTestUtils.setField(jwtUtil, "refreshExpirationTime", 600_000L);
}

@Test
void generaYValidaAccesstoken(){
    //generar un accestoken
    String token=jwtUtil.generateToken("fernando", "1", "ROLE_USER", "ferna@gmail.com");

//extraer el nombre de usuario y comparar
    assertEquals("fernando", jwtUtil.extractUsername(token));

//validamos contra el mismo nombre de usuario
assertTrue(jwtUtil.isTokenValid(token, "fernando"));

//validar contra otro usuario (debe ser falso)
assertFalse(jwtUtil.isTokenValid(token, "mario"));

}

@Test
void generaRefreshYExpTimeDisponible(){
    //generar refreshToken
    String refresh=jwtUtil.generateRefreshToken("fernando", "1", "ROLE_USER", "fernan@gmail.com");
assertNotNull(refresh);

//metodo de utilidad expuesto para la expiracion
assertEquals(600_000L, jwtUtil.getRefreshExpirationTime());
}

}
