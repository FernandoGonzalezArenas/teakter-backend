package com.teakter.user.service;

import com.teakter.user.dto.UserDto;
import com.teakter.user.dto.UserDtoEntrada;
import com.teakter.user.dto.UserPrincipal;
import com.teakter.user.entity.UserEntity;
import com.teakter.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

@Mock
    private UserRepository repository;

@Mock
private StorageService storageService;

@InjectMocks
    private UserServiceImpl service;

@Test
    void getProfile_UserExists_ReturnsUserDto(){
    UserEntity user=UserEntity.builder()
            .id(1L)
            .publicUserId("fskjtjo84hke")
            .username("testuser")
            .profilePictureUrl("portada.jpg")
            .privacyLikes(true)
            .privacySubs(false)
            .createdAt(Instant.now())
            .build();
    String publicUserId=user.getPublicUserId();

when(repository.findByPublicUserId(publicUserId)).thenReturn(Optional.of(user));
when(storageService.getPublicUrl(anyString())).thenReturn("http://localhost:9000/bucket-users/profiles/portada.jpg");

    UserDto result=service.getProfile(publicUserId);

    //verificaciones
    assertEquals("testuser", result.getUsername());
    assertTrue(result.getProfilePictureUrl().contains("/profiles"));
    verify(repository, times(1)).findByPublicUserId(publicUserId);
}

@Test
    void getProfile_UserNotFound_ThrowsException(){
    //simulamos que el usuario no existe en la db
    when(repository.findByPublicUserId("fskjjer8ehje")).thenReturn(Optional.empty());

    //verificar que lanza la esepcion esperada
    assertThrows(ResponseStatusException.class, () -> service.getProfile("fskjjer8ehje"));
}

@Test
@DisplayName("debe retornar 404 si el usuario no existe")
    void shouldThrouNotFound_WhenUserDoesNotExist(){
    UserPrincipal principal=new UserPrincipal("fskrjh8herhis", "user");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(repository.findByPublicUserId(anyString())).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> {
        service.updateProfile(new UserDtoEntrada("my description", "profile picture", false, true));
    });
}

@Test
    @DisplayName("ADMIN: debe borrar a el usuario si tiene permisos de administrador")
    void banUserInternal_ShouldDElete_WhenRequestIsUserAdmin(){
    UserPrincipal principal=new UserPrincipal("fsdht8p35ktiehk", "userAdmin");
    var auth=new UsernamePasswordAuthenticationToken(principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UserEntity user=UserEntity.builder()
                    .id(1L).publicUserId("fksjjti8ehkjedhk").build();
    String idBanUser=user.getPublicUserId();

    when(repository.findByPublicUserId(idBanUser)).thenReturn(Optional.of(user));

    assertDoesNotThrow(() -> service.banUserInternal(idBanUser));
    verify(repository).deleteById(user.getId());
}

@Test
    @DisplayName("ADMIN: retorna 403 si se quiere banear al usuario y no se es administrador")
    void banUserInternal_ShouldThrowForbidden_WhenUserNotIsAdmin(){
    UserPrincipal principal=new UserPrincipal("fshrh873herwj", "user");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.banUserInternal("jje84kjengu"));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
}

@Test
    @DisplayName("debe responder 404 si la cuenta que se quiere eliminar no existe")
    void banUserInternal_ShouldThrowNotFound_WhenUserDoesNotExist(){
    UserPrincipal principal=new UserPrincipal("fdsjdr873hkerjbrj", "userAdmin");
    var auth=new UsernamePasswordAuthenticationToken(principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(repository.findByPublicUserId("hert874hknre")).thenReturn(Optional.empty());

    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.banUserInternal("hert874hknre"));
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
}

}
