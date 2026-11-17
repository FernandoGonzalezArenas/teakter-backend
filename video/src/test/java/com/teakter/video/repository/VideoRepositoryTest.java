package com.teakter.video.repository;

import com.teakter.video.entity.VideoEntity;
import com.teakter.video.entity.VideoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
public class VideoRepositoryTest {

@Autowired
    private VideoRepository repository;

@Test
@DisplayName("debe buscar videos ignorando mayusculas o minusculas")
    void ShouldFindVideosByTitleCaseInsensitive(){
    repository.save(VideoEntity.builder()
                    .publicVideoId("fshfie8fyuiehuiehiuhdj")
            .userId("hu3gehukjebubydxh")
            .title("spring boot intro")
            .description("desc")
                    .duration(48L)
            .videoUrl("url")
                    .status(VideoStatus.READY)
            .build());
repository.save(VideoEntity.builder()
                .publicVideoId("fshfh78ehkjdbdgugedffj")
        .userId("her8tyerkh37ihej")
        .title("advanced java")
        .description("desc")
                .duration(74L)
        .videoUrl("url")
                .status(VideoStatus.READY)
        .build());

    Page<VideoEntity> result=repository.searchByTitle("spring", PageRequest.of(0, 10));
assertEquals(1, result.getTotalElements());
assertEquals("spring boot intro", result.getContent().get(0).getTitle());
assertTrue(result.getContent().stream()
        .noneMatch(v -> v.getTitle().contains("java")));
}

@Test
    @DisplayName("debe retornar los siguientes videos basados en la fecha (paginacion por cursor)")
    void shouldFindNextVideosBasedOnLastId(){
    Instant now=Instant.now();

//guardando videos
    VideoEntity v1=repository.save(VideoEntity.builder()
                    .publicVideoId("fskurf83u34h98yek")
            .userId("fshdfhieh33738")
            .title("video 1")
            .description("d")
                    .duration(37L)
            .videoUrl("u")
            .thumbnailUrl("t")
                    .status(VideoStatus.READY)
                    .createdAt(now.minusSeconds(3))
            .build());
    VideoEntity v2=repository.save(VideoEntity.builder()
                    .publicVideoId("skjf83438ty3ue")
            .userId("fshdfhieh33738")
            .title("video 2")
            .description("d")
                    .duration(48L)
            .videoUrl("u")
            .thumbnailUrl("t")
                    .status(VideoStatus.READY)
                    .createdAt(now.minusSeconds(2))
            .build());
    VideoEntity v3=repository.save(VideoEntity.builder()
                    .publicVideoId("fksriey74dduy")
            .userId("fshdfhieh33738")
            .title("video 3")
            .description("d")
                    .duration(58L)
            .videoUrl("u")
            .thumbnailUrl("t")
                    .status(VideoStatus.READY)
                    .createdAt(now.minusSeconds(1))
            .build());

//    Instant lastCreatedAt=v3.getCreatedAt();

    //pedimos videos con fecha menor a v3
    Pageable pageable=PageRequest.of(0, 2);
    List<VideoEntity> result=repository.findNextShortVideos(null, pageable);

    //validaciones
    assertEquals(2, result.size());
    assertEquals(v2.getTitle(), result.get(1).getTitle());
}

@Test
    @DisplayName("debe buscar videos filtrando por una lista de ids")
    void shouldFindVideosByIdIn(){
    VideoEntity v1=repository.save(VideoEntity.builder().publicVideoId("hjdh8tt4huheuge87gdj").userId("fshdfhieh33738").title("V1").description("d").duration(46L).videoUrl("U1").status(VideoStatus.READY).build());
    VideoEntity v2=repository.save(VideoEntity.builder().publicVideoId("skjhf8wyhi73yeu").userId("fshdfhieh33738").title("V2").description("d").duration(58L).videoUrl("U2").status(VideoStatus.READY).build());

    Pageable pageable=PageRequest.of(0, 2);

    List<VideoEntity> result=repository.findByIdIn(List.of(v1.getPublicVideoId(), v2.getPublicVideoId()), null, pageable);

    assertEquals(2, result.size());
}

@Test
    @DisplayName("debe obtener el feed de canales seguidos ordenados por fecha")
    void shouldFindByUserIdInOrderByCreatedAtDesc(){
    repository.save(VideoEntity.builder().publicVideoId("sfhjfh83khfiushkjfd").userId("shkhfiu3hjdjdd").title("canal a").description("d").duration(48L).videoUrl("U1").status(VideoStatus.READY).build());
    repository.save(VideoEntity.builder().publicVideoId("shfu3hfiuheieukrhi").userId("gsvkfhwuihig4hu4").title("canal b").description("d").duration(48L).videoUrl("U2").status(VideoStatus.READY).build());

    Pageable pageable=PageRequest.of(0, 2);

    //suponiendo que el usuario sigue a los id's de usuario creados arriba
    List<VideoEntity> result=repository.findByUserIdInOrderByCreatedAtDesc(List.of("shkhfiu3hjdjdd", "gsvkfhwuihig4hu4"), null, pageable);

    assertEquals(2, result.size());
}

@Test
    @DisplayName("debe borrar un video fisicamente de la base de datos")
    void shouldDeleteVideoFromDatabase(){
    VideoEntity video=repository.save(VideoEntity.builder()
            .publicVideoId("fshjfh8iwyufeih9e8dkj").userId("shhfi3yiewe7e").title("To Delete").description("d").duration(48L).videoUrl("u").build());
    Long id=video.getId();

    repository.deleteById(id);
    Optional<VideoEntity> deleted=repository.findById(id);

    assertTrue(deleted.isEmpty());
}

}
