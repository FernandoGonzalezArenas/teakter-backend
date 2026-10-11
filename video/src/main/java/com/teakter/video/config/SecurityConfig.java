package com.teakter.video.config;

import com.teakter.video.security.GatewayHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/videos/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                .requestMatchers("/videos/internal/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/videos/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/videos/shorts-feed/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/videos/long-feed/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/videos/interactions/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/videos/*/play").permitAll()
                .requestMatchers(HttpMethod.GET, "/videos/*").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
        .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new InMemoryUserDetailsManager();
    }

}
