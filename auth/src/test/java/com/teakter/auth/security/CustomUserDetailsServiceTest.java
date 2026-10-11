package com.teakter.auth.security;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.teakter.auth.entity.UserEntity;
import com.teakter.auth.repository.UserRepository;

public class CustomUserDetailsServiceTest {

@Test
void cargaUsuarioOK(){
    //mock de el repositorio
UserRepository repository=mock(UserRepository.class);

//entidad falsa
UserEntity user=UserEntity.builder()
.id(1L)
.username("fernando")
.password("{noop}123")
        .role("ROLE_USER")
.email("fer@mail.com")
.build();

//stub (caso) de el repositorio
when(repository.findByUsername("fernando")).thenReturn(Optional.of(user));

//servicio real con repository mockeado
CustomUserDetailsService svc=new CustomUserDetailsService(repository);
//invocacion
var ud=svc.loadUserByUsername("fernando");

//verificaciones
assertEquals("fernando", ud.getUsername());
assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
//verificar que el repositorio se llamo una vez
verify(repository, times(1)).findByUsername("fernando");
}
    
@Test
void usuarioNoExiste(){
    UserRepository repository=mock(UserRepository.class);
when(repository.findByUsername("mario")).thenReturn(Optional.empty());
CustomUserDetailsService svc=new CustomUserDetailsService(repository);
UsernameNotFoundException ex= assertThrows(UsernameNotFoundException.class, () -> svc.loadUserByUsername("mario"));
assertEquals("usuario no encontrado", ex.getMessage());
}

}
