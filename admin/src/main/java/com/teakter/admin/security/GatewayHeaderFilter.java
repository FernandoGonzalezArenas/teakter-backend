package com.teakter.admin.security;

import com.teakter.admin.dto.UserPrincipal;
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

@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
    String userId_str=request.getHeader("X-User-Id");
    String role_str=request.getHeader("X-User-Role");

    if (userId_str!=null && !userId_str.isEmpty()){
        try {
            Long userId=Long.parseLong(userId_str);
            UserPrincipal principal=new UserPrincipal(userId, null);

            List<SimpleGrantedAuthority> authorities= Collections.emptyList();
            if (role_str!=null && !role_str.isEmpty()){
                String finalRole=role_str.startsWith("ROLE_") ? role_str : "ROLE_"+role_str;

                authorities=List.of(new SimpleGrantedAuthority(finalRole));
            }

            var auth=new UsernamePasswordAuthenticationToken(
                    principal, null,
authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }catch (NumberFormatException e){
logger.error("formato de X-User-Id invalido: "+userId_str);
        }
    }
    filterChain.doFilter(request, response);
}

}
