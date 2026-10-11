package com.teakter.comments.service;

import com.teakter.comments.dto.CommentDtoEntrada;
import com.teakter.comments.dto.CommentsDto;
import com.teakter.comments.dto.UpdateCommentDto;
import com.teakter.comments.dto.UserPrincipal;
import com.teakter.comments.entity.CommentEntity;
import com.teakter.comments.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

@Mock
    private CommentRepository repository;

@InjectMocks
    private CommentServiceImpl service;

private Long userId=1L;
private String username="fernando";

@BeforeEach
void setupSecurityContext(){
    UserPrincipal principal=new UserPrincipal(userId, username);
    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    void saveComments_ShouldReturnCommentsDto_ValidImput(){
    CommentDtoEntrada entrada=new CommentDtoEntrada(1L, "contenido de prueba");
    String username="fernando";

    CommentEntity entitySaved=new CommentEntity();
    entitySaved.setId(1L);
    entitySaved.setUsernameAuthor(username);
    entitySaved.setVideoId(1L);
    entitySaved.setUserId(1L);
    entitySaved.setContent("contenido de prueba");

    Instant now=Instant.now();
    entitySaved.setCreatedAt(now);
    entitySaved.setUpdatedAt(now);

when(repository.save(any(CommentEntity.class))).thenReturn(entitySaved);

CommentsDto resultado=service.saveComments(entrada);
assertNotNull(resultado);
assertEquals(username, resultado.getUsernameAuthor());
verify(repository, times(1)).save(any(CommentEntity.class));
}

@Test
    void deleteComment_ShouldThrowForbidden_WhenUserIsNotAuthor(){
Long commentId=1L;
String intruderUser="hacker123";
CommentEntity existingComment=new CommentEntity();
existingComment.setUsernameAuthor("originalAuthor");
when(repository.findById(commentId)).thenReturn(Optional.of(existingComment));

    ResponseStatusException exception=assertThrows(ResponseStatusException.class, () -> {
service.deleteComment(commentId);
    });
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
}

@Test
    void editComment_ShouldThrowNotFound_WhenCommentDoesNotExist(){
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> {
        service.editComment(99L, new UpdateCommentDto("nuevo"));
    });
}

@Test
@DisplayName("debe contar los comentarios de un video")
void shouldCountCommentsInVideo(){
    when(repository.countByVideoId(1L)).thenReturn(67L);

    Long count=service.countCommentsInVideo(1L);

    //validacion
    assertEquals(67L, count);
}

@Test
    @DisplayName("debe borrar los comentarios de un video")
    void shouldDeleteCommentsByVideoId(){
    assertDoesNotThrow(() -> service.deleteCommentsWithVideoId(1L));
    verify(repository).deleteByVideoId(1L);
}

}
