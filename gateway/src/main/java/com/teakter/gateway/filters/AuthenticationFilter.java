package com.teakter.gateway.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.teakter.gateway.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

private final JwtUtil jwtUtil;

@Autowired
    public AuthenticationFilter(JwtUtil jwtUtil){
    this.jwtUtil=jwtUtil;
}

@Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
    System.out.println("ingresando a el metodo para validar el token JWT y enviar los datos de el usuario a los demas microservicios atravez de los hencabezados");
    ServerHttpRequest request= exchange.getRequest();
String path=request.getURI().getPath();
String method=request.getMethod().name();
    System.out.println("Filtro ejecución | Método: " + method + " | URI: " + path + " | ");

//si es options dejar pasar directamente para que el CORS responda
    if (HttpMethod.OPTIONS.equals(request.getMethod())){
        return chain.filter(exchange);
    }

//rutas permitidas sin token
    if (
        path.startsWith("/auth/register") ||
            path.startsWith("/auth/login") ||
            path.startsWith("/auth/verify") ||
            path.startsWith("/auth/refresh") ||
            path.startsWith("/auth/logout") ||
            path.startsWith("/actuator") ||
    path.contains("/v3/api-docs") ||
    path.contains("/swagger-ui") ||
    path.contains("/webjars") ||
    path.startsWith("/storage")){
        System.out.println("dentro de el if de las rutas permitidas sin token en el mmicroservicio gateway");
        return chain.filter(exchange);
    }

    //definicion de rutas publicas GET
    boolean isPublicGet=method.equals("GET") && (path.matches("/videos/interactions/\\d+") ||
        path.matches("/comentarios/\\d+") ||
        path.startsWith("/like/") ||
//        path.matches("/like/\\d+/status") ||
        path.matches("/users/\\d+") ||
        path.startsWith("/subs/") ||
//        path.matches("/subs/\\d+/status") ||
        path.matches("/videos/\\d+/play") ||
        path.matches("/videos/\\d+") ||
        path.equals("/videos/shorts-feed") ||
        path.equals("/videos/long-feed") ||
        path.equals("/videos/search")
);

//obtener el token
String token=extractToken(request);
    System.out.println("token JWT extraido correctamente: "+ token);

//si no hay token pero es ruta publica se deja pasar
    if (token == null){
if (isPublicGet){
    System.out.println("ingresando a el if que permite las rutas publicas en el microservicio gateway");
    return chain.filter(exchange);
}
        System.out.println("acceso no permitido por no ser ruta publica y no tener token en gateway");
return unauthorizedResponse(exchange, "no hay token para el recurso protegido");
}

try {
    //validar token
    if (jwtUtil.isTokenExpired(token)){
        return unauthorizedResponse(exchange, "token expirado");
    }
String userId=jwtUtil.extractUserId(token);
String role=jwtUtil.extractRole(token);

boolean isReportCreation = method.equals("POST") && path.startsWith("/admin/reports/");

if (path.startsWith("/admin") && !isReportCreation && !"ROLE_ADMIN".equals(role)){
    return unauthorizedResponse(exchange, "acceso denegado, se requieren privilegios de administrador");
}

    String username=jwtUtil.extractUsername(token).orElse("unknown");
    System.out.println("los datos de el usuario obtenidos de el token en el filtro de el gateway son: \nuserId = "+userId+"\nrole = "+role+"\nusername = "+username+"\n");

    //mutar la peticion para que el token siga y agregar el userId limpio
    ServerWebExchange mutatedExchange=exchange.mutate()
        .request(exchange.getRequest().mutate()
            .header("X-User-Id", userId)
            .header("X-User-Role", role)
            .header("X-User-Name", username)
            .build())
        .build();
    System.out.println("datos de el usuario obtenidos de el token y agregados a la peticion | AuthenticationFilter/gateway");

return chain.filter(mutatedExchange);
}catch (JWTVerificationException e){
    //si venia un token invalido pero la ruta es publica se deja pasar
    if (isPublicGet){
        return chain.filter(exchange);
    }

return unauthorizedResponse(exchange, "Invalid token");
}
}

//metodo para extraer el token de las cookies o de la cabezera authorization
    private String extractToken(ServerHttpRequest request){
    //buscar en la cookie "accessToken"
        HttpCookie cookie=request.getCookies().getFirst("accessToken");
        if (cookie != null && !cookie.getValue().isEmpty()){
            System.out.println("token extraido de cookie en microservicio gateway");
            return cookie.getValue();
        }

        //buscar en el Header Authorization "Bearer <token>"
        String authHeader= request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
if (authHeader != null && authHeader.startsWith("Bearer ")){
    System.out.println("token extraido de el header en microservicio gateway");
    return authHeader.substring(7);
}

        System.out.println("no hay token y no se extrae de ningun lado");
return null;
    }

private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message){
    ServerHttpResponse response= exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
String json= String.format("{\"error\": \"%s\"}", message);

    byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer=response.bufferFactory().wrap(bytes);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    return response.writeWith(Mono.just(buffer))
        .then(Mono.defer(response::setComplete));
}

@Override
    public int getOrder(){
    return -1;
}

}
