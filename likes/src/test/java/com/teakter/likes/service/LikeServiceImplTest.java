package com.teakter.likes.service;

import com.teakter.likes.Service.LikeServiceImpl;
import com.teakter.likes.dto.IdsDto;
import com.teakter.likes.dto.UserPrincipal;
import com.teakter.likes.dto.VideoLikeStatusDto;
import com.teakter.likes.entity.LikeEntity;
import com.teakter.likes.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceImplTest {

@Mock
    private LikeRepository repository;
@InjectMocks
    private LikeServiceImpl service;

private Long videoId=100L;
private Long userId=1L;

@BeforeEach
void setupSecurityContext(){
    //simulamos que Spring Security ya tiene al usuario autenticado
    UserPrincipal principal=new UserPrincipal(userId, "user-likes");
    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    void toggleLike_ShouldCreateLike_WhenNotExists(){
    //el usuario esta autenticado y no ha dado like antes
    when(repository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());

    //ejecutar el toggle
    boolean resultado=service.toggleLike(videoId);

    //validaciones (debe devolver true y haver llamado a save una vez)
    assertTrue(resultado);
    verify(repository, times(1)).save(any(LikeEntity.class));
}

@Test
    void toggleLike_ShouldDeleteLike_WhenAlreadyExists(){
    //dar like agregando registro a la base de datos
    LikeEntity existingLike=LikeEntity.builder()
            .id(1L)
            .userId(userId)
            .videoId(videoId)
            .build();
    when(repository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(existingLike));

    //quitar el like llamando a el toggle
    boolean resultado=service.toggleLike(videoId);

    //validaciones (el resultado debe ser false y llamar a delete una vez)
    assertFalse(resultado);
    verify(repository, times(1)).delete(existingLike);
}

@Test
void shouldReturnNumberOfLikes(){
    when(repository.countByVideoId(1L)).thenReturn(44L);

    Long likes=service.countLikes(1L);

    //validacion
    assertEquals(44L, likes);
}

@Test
    void getVideoLikeStatus_ShouldReturnCorrectStatus_ForAuthenticatedUser(){
    when(repository.countByVideoId(videoId)).thenReturn(15L);
    when(repository.existsByUserIdAndVideoId(userId, videoId)).thenReturn(true);

    //llamada a el metodo
    VideoLikeStatusDto status=service.getVideoLikeStatus(videoId);

    //validaciones
    assertEquals(15L, status.getTotalLikes());
    assertTrue(status.isLikedByUser());
}

@Test
    void getVideoLikeStatus_ShouldReturnFalse_ForGuestUser(){
//limpeamos el contexto para simular que no hay nadie logeado
    SecurityContextHolder.clearContext();

    when(repository.countByVideoId(videoId)).thenReturn(15L);

    VideoLikeStatusDto status=service.getVideoLikeStatus(videoId);

    //validaciones
    assertEquals(15L, status.getTotalLikes());
    assertFalse(status.isLikedByUser());
}

@Test
    void getLikesVideosByUserId_ShouldReturnIdsDto(){
    LikeEntity like1=LikeEntity.builder().videoId(10L).build();
    LikeEntity like2=LikeEntity.builder().videoId(20L).build();
    when(repository.findByUserId(1L)).thenReturn(List.of(like1, like2));

    IdsDto result=service.getLikesVideosByUserId(1L);

    //validaciones
    assertNotNull(result);
    assertEquals(2, result.getIds().size());
assertTrue(result.getIds().contains(10L));
assertTrue(result.getIds().contains(20L));
verify(repository, times(1)).findByUserId(userId);
}

@Test
    @DisplayName("debe eliminar los likes de un video")
    void shouldDeleteLikesByVideoId(){
    assertDoesNotThrow(() -> service.deleteLikesVideo(1L));
    verify(repository).deleteByVideoId(1L);
}

}
