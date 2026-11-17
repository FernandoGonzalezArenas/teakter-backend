package com.teakter.video.service;

import com.teakter.video.dto.*;
import com.teakter.video.entity.VideoEntity;
import com.teakter.video.repository.VideoRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MinioVideoServiceTest {

@Mock
    private VideoRepository repository;
@Mock
    private InteractionsService interactionsService;

@Mock
private LikeService likeService;

@Mock
private SubscriptionService subscriptionService;

@Mock
private UserService userService;

@Mock
private RabbitTemplate rabbitTemplate;

@Mock
    private MinioClient minioClient;
private MinioVideoServiceImpl service;

private final String userId="jsfo8iu38he7ehd";

@BeforeEach
    void setup(){
    //instanciamos la verdadera implementacion de la clase
    service=new MinioVideoServiceImpl(repository, interactionsService, likeService, subscriptionService, userService, rabbitTemplate, minioClient);

    //inyectamos valores de value con ReflectionTestUtils
    ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(service, "minioUrl", "http://localhost:9000");

    //agregamos el principal a el contexto de spring
    UserPrincipal principal=new UserPrincipal(userId, "user-minio");
    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    @DisplayName("debe generar una URL prefirmada (UploadUrlResponse)")
    void shouldGenerateUploadUrl() throws Exception{
    //nombre de archivo
    String filename="vacaciones.mp4";

    //se simula la llamada compleja a minioClient
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://signed-url.com?signature=xyz");

    UploadUrlResponse response=service.generateUploadUrl(filename);

    //validaciones
    assertNotNull(response);
    assertEquals("http://signed-url.com?signature=xyz", response.uploadUrl());
    assertTrue(response.finalFileName().contains(filename));
}

@Test
@DisplayName("debe generar una URL firmada para la subida de la miniatura")
void shouldGenerateUploadUrlThumb() throws Exception{
    String filename="portada.jpg";
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://localhost:8080/storage/thumbnails/uuid-portada.png");

    UploadUrlResponse response=service.generateUploadUrlThumb(filename);

    assertNotNull(response);
    assertEquals("http://localhost:8080/storage/thumbnails/uuid-portada.png", response.uploadUrl());
    verify(minioClient).getPresignedObjectUrl(any());
}

@Test
    @DisplayName("minio: debe generar VideoPlaybackDto con URL firmada de lectura")
    void shouldGetVideoForPlaybackMinio() throws Exception{
    VideoEntity video=VideoEntity.builder()
            .id(1L).publicVideoId("shfhi8ehuehtiue").userId("fshhdfieyieh7e").title("Video Test")
            .duration(35L)
            .videoUrl("clip.mp4")
            .build();
    when(repository.findByPublicVideoId("shfhi8ehuehtiue")).thenReturn(Optional.of(video));
when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://signed-playback-url.com");

    VideoPlaybackDto result=service.getVideoForPlayback("shfhi8ehuehtiue");

    //validaciones
    assertNotNull(result);
    assertEquals("http://signed-playback-url.com", result.getVideoUrl());
}

@Test
    @DisplayName("debe obtener los videos gustados de el usuario")
    void shouldGetLikedVideos(){
    //usuario simulado
    UserDto user = UserDto.builder().id("sjf8iwy8he7eyte").username("fernando").privacyLikes(true).build();

    when(userService.getProfile("sjf8iwy8he7eyte")).thenReturn(user);
    when(likeService.getLikedVideos(anyString())).thenReturn(List.of("sfhhfi734yuegue"));

    VideoEntity video=VideoEntity.builder()
                    .title("video liked")
                            .description("desc")
            .duration(36L)
                                    .videoUrl("url")
                                            .thumbnailUrl("thumb")
            .createdAt(Instant.now())
                                                    .build();
    when(repository.findByIdIn(List.of("sfhhfi734yuegue"), null, PageRequest.of(0, 5))).thenReturn(List.of(video));

List<VideoDto> feed=service.getVideosByIds("sjf8iwy8he7eyte", null, 5);

//validaciones
    assertFalse(feed.isEmpty());
    verify(repository).findByIdIn(List.of("sfhhfi734yuegue"), null, PageRequest.of(0, 5));
}

@Test
@DisplayName("debe verificar el mensaje de error si el servicio de almacenamiento minio faya")
    void shouldThrowExceptionWhenMinioFails() throws Exception{
    when(minioClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("minio down"));

    assertThrows(RuntimeException.class, () -> service.generateUploadUrl("video.mp4"));
}

}
