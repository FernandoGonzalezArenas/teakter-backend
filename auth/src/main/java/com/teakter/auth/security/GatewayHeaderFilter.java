package com.teakter.auth.security;

import com.teakter.auth.dto.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private int contador=0;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        contador++;
        String path = request.getRequestURI();
        String method = request.getMethod();
        String userIdHeader = request.getHeader("X-User-Id");

        System.out.println("(" + contador + ").- dentro de el metodo de GatewayHeaderFilter para extraer los datos de el header enviado por el gateway y agregarlos al contexto");
        logger.info("Filtro ejecución (" + contador + ") | Método: " + method + " | URI: " + path + " | X-User-Id: " + userIdHeader);

        String userId_str=request.getHeader("X-User-Id");
        String role_str=request.getHeader("X-User-Role");
        System.out.println("los datos pasados por el gateway de la ruta "+request.getPathInfo()+" son: \nuserId(String) = "+userId_str+"\nrole(String) = "+ role_str+ "\n");

        if (userId_str!=null && !userId_str.isEmpty()){
            System.out.println("ingresando a el if que garantiza que los datos no vengan nulos: \nuserId(String) = "+userId_str+"\nrole(String) = "+ role_str+ "\n");
            try {
                Long userId = Long.parseLong(userId_str);
                System.out.println("userId convertido a long = "+ userId);
                UserPrincipal principal=new UserPrincipal(userId, null);

                //convertimos el String de el role en una autoridad de spring
                List<SimpleGrantedAuthority> authorities=Collections.emptyList();
                if (role_str!=null && !role_str.isEmpty()){
                    String finalRole=role_str.startsWith("ROLE_") ? role_str : "ROLE_"+role_str;

                    authorities=List.of(new SimpleGrantedAuthority(finalRole));
                    System.out.println("role convertido a una autoridad de spring");
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("datos de el usuario asignados al contexto correctamente.\nID de usuario = "+ userId);
            }catch (NumberFormatException e){
                logger.error("formato de X-User-Id invalido: "+ userId_str);
            }
        } else {
            System.out.println("el userId viene nulo y no entro al if para asignar los datos al contexto");
        }
        filterChain.doFilter(request, response);
    }

}
