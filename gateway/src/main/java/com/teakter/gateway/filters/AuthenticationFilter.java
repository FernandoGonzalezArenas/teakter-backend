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
    private static final String standarUrl="/media/api/v1";

@Autowired
    public AuthenticationFilter(JwtUtil jwtUtil){
    this.jwtUtil=jwtUtil;
}

@Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
    ServerHttpRequest request= exchange.getRequest();
String path=request.getURI().getPath();
String method=request.getMethod().name();

//si es options dejar pasar directamente para que el CORS responda
    if (HttpMethod.OPTIONS.equals(request.getMethod())){
        return chain.filter(exchange);
    }

//rutas permitidas sin token
    if (
        path.startsWith(standarUrl+"/auth/register") ||
            path.startsWith(standarUrl+"/auth/login") ||
            path.startsWith(standarUrl+"/auth/verify") ||
            path.startsWith(standarUrl+"/auth/refresh") ||
            path.startsWith(standarUrl+"/auth/logout") ||
            path.startsWith(standarUrl+"/actuator") ||
    path.contains("/v3/api-docs") ||
    path.contains("/swagger-ui") ||
    path.contains("/webjars") ||
    path.startsWith("/storage")){
        return chain.filter(exchange);
    }

    //definicion de rutas publicas GET
    boolean isPublicGet=method.equals("GET") &&
        (path.matches(standarUrl+"/videos/interactions/[a-zA-Z0-9-]+") ||
        path.matches(standarUrl+"/comentarios/[a-zA-Z0-9-]+") ||
        path.startsWith(standarUrl+"/like/") ||
        path.matches(standarUrl+"/users/[a-zA-Z0-9-]+") ||
        path.startsWith(standarUrl+"/subs/") ||
        path.matches(standarUrl+"/videos/[a-zA-Z0-9-]+/play") ||
        path.matches(standarUrl+"/videos/[a-zA-Z0-9-]+") ||
        path.equals(standarUrl+"/videos/shorts-feed") ||
        path.equals(standarUrl+"/videos/long-feed") ||
        path.equals(standarUrl+"/videos/search")
);

    //excluír a las rutas de subida de archivos de las rutas get permitidas sin token
    if (path.contains("/upload")){
        isPublicGet=false;
    }

//obtener el token
String token=extractToken(request);

//si no hay token pero es ruta publica se deja pasar
    if (token == null){
if (isPublicGet){
    return chain.filter(exchange);
}
return unauthorizedResponse(exchange, "no hay token para el recurso protegido");
}

try {
    //validar token
    if (jwtUtil.isTokenExpired(token)){
        return unauthorizedResponse(exchange, "token expirado");
    }
String userId=jwtUtil.extractUserId(token);
String role=jwtUtil.extractRole(token);

boolean isReportCreation = method.equals("POST") && path.startsWith(standarUrl+"/admin/reports/");

if (path.startsWith(standarUrl+"/admin") && !isReportCreation && !"ROLE_ADMIN".equals(role)){
    return unauthorizedResponse(exchange, "acceso denegado, se requieren privilegios de administrador");
}

    String username=jwtUtil.extractUsername(token).orElse("unknown");

    //mutar la peticion para que el token siga y agregar el userId limpio
    ServerWebExchange mutatedExchange=exchange.mutate()
        .request(exchange.getRequest().mutate()
            .header("X-User-Id", userId)
            .header("X-User-Role", role)
            .header("X-User-Name", username)
            .build())
        .build();

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
            return cookie.getValue();
        }

        //buscar en el Header Authorization "Bearer <token>"
        String authHeader= request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
if (authHeader != null && authHeader.startsWith("Bearer ")){
    return authHeader.substring(7);
}

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
