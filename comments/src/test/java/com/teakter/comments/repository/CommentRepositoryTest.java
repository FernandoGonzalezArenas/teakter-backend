package com.teakter.comments.repository;

import com.teakter.comments.entity.CommentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CommentRepositoryTest {

@Autowired
    private CommentRepository repository;

@Autowired
private TestEntityManager entityManager;

@Test
    void findNextComments_ShouldReturnOnlyCommentsWithLowerId(){
    repository.save(CommentEntity.builder().videoId(1L).userId(1L).usernameAuthor("usuario1").content("comentario 1").build());
    repository.save(CommentEntity.builder().videoId(1L).userId(1L).usernameAuthor("usuario1").content("comentario 2").build());
    CommentEntity ref=repository.save(CommentEntity.builder().videoId(1L).userId(1L).usernameAuthor("usuario1").content("comentario 3").build());

    Long lastId=ref.getId();

    List<CommentEntity> result=repository.findNextComments(1L, lastId, PageRequest.of(0, 10));

//validaciones
    assertEquals(2, result.size());
    assertTrue(result.get(0).getId() < lastId);
}

    @Test
void shouldCountCommentsByVideoId(){
repository.save(CommentEntity.builder().videoId(1L).userId(1L).usernameAuthor("usuario1").content("comentario 1").build());
        repository.save(CommentEntity.builder().videoId(1L).userId(1L).usernameAuthor("usuario1").content("comentario 2").build());

    Long count =repository.countByVideoId(1L);

    //validacion
    assertEquals(2, count);
}

@Test
    @DisplayName("debe eliminar fisicamente comentarios de un video")
    void shouldDeleteCommentsOfVideo(){
    CommentEntity comment=repository.save(CommentEntity.builder()
            .videoId(1L).userId(1L).usernameAuthor("yo").content("mi comentario").build());
    Long id=comment.getId();

    entityManager.flush();

    repository.deleteByVideoId(1L);

    entityManager.clear();
    Optional<CommentEntity> deleted=repository.findById(id);

    assertTrue(deleted.isEmpty());
}

}
