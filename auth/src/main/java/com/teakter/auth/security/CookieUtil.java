package com.teakter.auth.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

public ResponseCookie createAccessTokenCookie(String token){
return createCookie("accessToken", token, "/", 15 * 60);
}

public ResponseCookie createRefreshTokenCookie(String token){
    return createCookie("refreshToken", token, "/auth", 7 * 24 * 60 * 60);
}

public ResponseCookie deleteAccessTokenCookie(){
    return deleteCookie("accessToken", "/");
}

public ResponseCookie deleteRefreshTokenCookie(){
    return deleteCookie("refreshToken", "/auth");
}

private ResponseCookie createCookie(String name, String value, String path, long maxAgeSeconds){
    return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(false) //cambiar a true cuando se suba a produccion y se use https
            .path(path)
            .maxAge(maxAgeSeconds)
            .sameSite("Lax")
            .build();
}

private ResponseCookie deleteCookie(String name, String path){
    return ResponseCookie.from(name)
            .httpOnly(true)
            .path(path)
            .maxAge(0)
            .sameSite("Lax")
            .build();
}

}
