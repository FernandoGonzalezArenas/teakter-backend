package com.teakter.auth.controller;

import com.teakter.auth.dto.*;
import com.teakter.auth.security.CookieUtil;
import com.teakter.auth.security.JwtUtil;
import com.teakter.auth.service.AuthService;
import com.teakter.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "gestiona el registro y acceso de usuarios, mediante la generacion de tokens JWT para acceso y refresco")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;


    @Operation(summary = "registrar a el usuario",
            description = "registra los datos de el usuario en la base de datos incluyendo su rol de acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "registro exitoso"),
            @ApiResponse(responseCode = "400", description = "datos incompletos o usuario ya existente")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        System.out.println("ingresando a el endpoint de registro en el microservicio auth");
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("usuario creado correctamente");
    }

    @Operation(summary = "login de usuarios",
            description = "inicio de cesion de los usuarios con su username y password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "login exitoso",
                    content=@Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "acceso no autorizado por credenciales invalidas"),
            @ApiResponse(responseCode = "500", description = "error interno de el servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("ingresando a el endpoint de login en el microservicio auth");
        AuthResponse response=authService.login(request);

        ResponseCookie accessCookie= cookieUtil.createAccessTokenCookie(response.getAccessToken());
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(response.getRefreshToken());

        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body("login exitoso");
    }

    @Operation(summary = "verificar correo de usuario",
            description = "valida el token enviado por correo para activar la cuenta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "cuenta verificada con exito"),
            @ApiResponse(responseCode = "400", description = "token invalido o expirado")
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token){
        System.out.println("ingresando a el endpoint de verificar correo en el microservicio auth");
        authService.verifyAccount(token);
        return ResponseEntity.ok("cuenta verificada correctamente, ya puedes iniciar sesion");
    }

    @Operation(summary = "refresco de tokens (renovacion)", description = "se piden tokens de acceso y refresco nuevos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "renovacion de tokens exitosa",
                    content=@Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "acceso no autorizado para refrescar tokens por token invalido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken){
        if (refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no se proporciono el refreshToken");
        }

        AuthResponse response=refreshTokenService.refreshToken(refreshToken);
        ResponseCookie accessCookie=cookieUtil.createAccessTokenCookie(response.getAccessToken());
        ResponseCookie refreshCookie=cookieUtil.createRefreshTokenCookie(response.getRefreshToken());

        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body("token renovado exitosamente");
    }

    @Operation(summary = "cerrar cesion de usuario",
            description = "cerrar la cesion de el usuario borrando los tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "cesion cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "acceso no autorizado para cerrar la cesion por token invalido")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken){
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        ResponseCookie cleanAccess=cookieUtil.deleteAccessTokenCookie();
        ResponseCookie cleanRefresh=cookieUtil.deleteRefreshTokenCookie();

        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cleanAccess.toString());
        headers.add(HttpHeaders.SET_COOKIE, cleanRefresh.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body("sesion cerrada correctamente");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){
        System.out.println("ingresando a el endpoint de datos de el usuario (/me) en el microservicio auth");
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        System.out.println("se encontró un usuario autenticado");

        DataJwtUser jwtUser=authService.getUserById(principal.userId());
        System.out.println("se retornan los datos de el usuario autenticado");

        return ResponseEntity.ok(jwtUser);
    }

}
