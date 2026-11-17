package com.teakter.likes.repository;

import com.teakter.likes.entity.LikeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class LikeRepositoryTest {

@Autowired
    private LikeRepository repository;

@Autowired
private TestEntityManager entityManager;

@Test
    void shouldSaveLikeSuccessfully(){
    LikeEntity like=LikeEntity.builder()
            .userId("sjafh89ehkeh")
            .videoId("sdjhri34yuh8733")
            .build();

    LikeEntity saved=repository.save(like);

    //validaciones
    assertNotNull(saved.getId());
    assertTrue(repository.existsByUserIdAndVideoId("sjafh89ehkeh", "sdjhri34yuh8733"));
}

@Test
    void shouldFailWhenDuplicateLikeIsInserted(){
    LikeEntity like1=LikeEntity.builder()
            .userId("sjafh89ehkeh").videoId("sjhi8ehti54y48").build();
    repository.saveAndFlush(like1);

    //intentamos guardar otra vez exactamente el mismo like
    LikeEntity like2=LikeEntity.builder()
            .userId("sjafh89ehkeh").videoId("sjhi8ehti54y48").build();

    //debe lanzar una excepcion de integridad de datos al querer guardar el mismo like otra vez
    assertThrows(DataIntegrityViolationException.class, () -> {
        repository.saveAndFlush(like2);
    });
}

@Test
    void ShouldCountLikesByVideoId(){
    //simular likes en videos
    repository.save(LikeEntity.builder().userId("sjafh89ehkeh").videoId("fshdhr734yuehitrd").build());
    repository.save(LikeEntity.builder().userId("fhsdhriu34uo9eru98").videoId("fshdhr734yuehitrd").build());

    Long count=repository.countByVideoId("fshdhr734yuehitrd");

    //validacion
    assertEquals(2, count);
}

@Test
    void shouldFindAllLikesByUserId(){
    repository.save(LikeEntity.builder().userId("fhsdhf7ihf9834y7e").videoId("gsjkjto85eu98teiu").build());
    repository.save(LikeEntity.builder().userId("fhsdhf7ihf9834y7e").videoId("hdlfug8954up994u98rtlk").build());
    repository.save(LikeEntity.builder().userId("jhiogu458ojro845oitrhi").videoId("hfdbfgoiu45ojeo8thekhgek").build());

    List<LikeEntity> result=repository.findByUserId("fhsdhf7ihf9834y7e");

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(l -> l.getVideoId().equals("gsjkjto85eu98teiu")));
    assertTrue(result.stream().anyMatch(l -> l.getVideoId().equals("hdlfug8954up994u98rtlk")));
}

@Test
    @DisplayName("debe borrar los likes fisicamente de la base de datos con respecto a el video")
    void shouldDeleteLikesByVideoId(){
    LikeEntity like=repository.save(LikeEntity.builder()
            .videoId("hdlfug8954up994u98rtlk").userId("jdsdhoiru823hk4wiur3h4i").build());
    Long id=like.getId();

entityManager.flush();

repository.deleteByVideoId("hdlfug8954up994u98rtlk");

entityManager.clear();
    Optional<LikeEntity> deleted=repository.findById(id);

    assertTrue(deleted.isEmpty());
}

}
