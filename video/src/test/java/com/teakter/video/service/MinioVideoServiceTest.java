package com.teakter.video.service;

import com.teakter.video.dto.*;
import com.teakter.video.entity.VideoEntity;
import com.teakter.video.repository.VideoRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

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

private Long userId=55L;

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
    @DisplayName("debe guardar metadata y construir URL correctamente")
    void shouldSaveMetadataAndBuildUrl(){
    VideoDtoEntrada entrada=VideoDtoEntrada.builder()
            .title("mi video")
            .description("descripcion valida")
            .duration(257L)
            .filename("video.mp4")
            .thumbnailUrl("/thumb.jpg")
            .build();

//capturamos lo que se envia al repositorio para verificarlo
ArgumentCaptor<VideoEntity> captor=ArgumentCaptor.forClass(VideoEntity.class);

    VideoDto result=service.saveVideoMetadata(entrada);

    //verificaciones y validaciones
    verify(repository).save(captor.capture());
    VideoEntity savedEntity=captor.getValue();
    assertEquals("mi video", savedEntity.getTitle());
    assertEquals(55L, savedEntity.getUserId());
    //verificamos la logica de url
    assertEquals("video.mp4", savedEntity.getVideoUrl());
    //verificamos el retorno DTO
    assertEquals("mi video", result.getTitle());
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
@DisplayName("debe buscar videos por titulo")
void shouldSearchVideosByTitle(){
    String keyword="java";
    String type="shorts";
    VideoEntity entity=VideoEntity.builder().userId(1L).title("tutorial java").duration(58L).build();
    Page<VideoEntity> page=new PageImpl<>(List.of(entity));

    when(repository.searchShortsByTitle(eq(keyword), any(PageRequest.class))).thenReturn(page);

    Page<VideoDto> result=service.searchVideosByTitle(keyword, type, 0, 10);

    assertEquals(1, result.getTotalElements());
    assertEquals("tutorial java", result.getContent().get(0).getTitle());
}

@Test
@DisplayName("debe mostrar correctamente el feed con la busqueda por cursor")
void shouldGetFeedCorrectly(){
    VideoEntity v1=VideoEntity.builder().id(4L).userId(2L).title("v1").duration(43L).build();
    when(repository.findNextShortVideos(anyLong(), any(PageRequest.class))).thenReturn(List.of(v1));

    List<VideoDto> result=service.getShortsFeed(10L, 5);

    assertEquals(1, result.size());
    verify(repository).findNextShortVideos(eq(10L), any(PageRequest.class));
}

@Test
    @DisplayName("minio: debe generar VideoPlaybackDto con URL firmada de lectura")
    void shouldGetVideoForPlaybackMinio() throws Exception{
    VideoEntity video=VideoEntity.builder()
            .id(1L).title("Video Test")
            .duration(35L)
            .videoUrl("clip.mp4")
            .build();
    when(repository.findById(1L)).thenReturn(Optional.of(video));
when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://signed-playback-url.com");

    VideoPlaybackDto result=service.getVideoForPlayback(1L);

    //validaciones
    assertNotNull(result);
    assertEquals("http://signed-playback-url.com", result.getVideoUrl());
}

@Test
    @DisplayName("debe obtener los videos gustados de el usuario")
    void shouldGetLikedVideos(){
    //usuario simulado
    UserDto user = UserDto.builder().id(1L).username("fernando").privacyLikes(true).build();

    when(userService.getProfile(1L)).thenReturn(user);
    when(likeService.getLikedVideos(anyLong())).thenReturn(List.of(10L));

    VideoEntity video=VideoEntity.builder()
                    .title("video liked")
                            .description("desc")
            .duration(36L)
                                    .videoUrl("url")
                                            .thumbnailUrl("thumb")
                                                    .build();
    when(repository.findByIdIn(List.of(10L), null, PageRequest.of(0, 5))).thenReturn(List.of(video));

List<VideoDto> feed=service.getVideosByIds(1L, null, 5);

//validaciones
    assertFalse(feed.isEmpty());
    verify(repository).findByIdIn(List.of(10L), null, PageRequest.of(0, 5));
}

@Test
    @DisplayName("ADMIN: debe eliminar el video si el usuario tiene ROLE_ADMIN")
    void deleteVideoInternal_ShouldDelete_WhenUserIsAdmin(){
    Long videoId=1L;

    //simulamos usuario con role admin
    UserPrincipal principal=new UserPrincipal(99L, "userAdmin");
    var auth=new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);
when(repository.existsById(videoId)).thenReturn(true);

assertDoesNotThrow(() -> service.deleteVideoInternal(videoId));
verify(repository).deleteById(videoId);
}

@Test
    @DisplayName("ADMIN: debe lanzar 403 al intentar borrar si el usuario no es admin")
    void deleteVideoInternal_ShouldThrowForbidden_WhenUserNotIsAdmin(){
    UserPrincipal principal=new UserPrincipal(1L, "user");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    ResponseStatusException ex=assertThrows(ResponseStatusException.class, () -> service.deleteVideoInternal(1L));
    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
}

@Test
    @DisplayName("ADMIN: debe retornar detalles completos de el video para el admin")
    void videoInternalDetails_ShouldReturnDetails_WhenUserIsAdmin() throws Exception{
List<Long> videoIds=List.of(1L, 2L, 3L);
UserPrincipal principal=new UserPrincipal(99L, "userAdmin");
var auth=new UsernamePasswordAuthenticationToken(
        principal,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
SecurityContextHolder.getContext().setAuthentication(auth);

VideoEntity v1=VideoEntity.builder()
        .id(1L).title("Video Admin1").duration(48L).videoUrl("URL-Original1").build();
    VideoEntity v2=VideoEntity.builder()
            .id(2L).title("Video Admin2").duration(78L).videoUrl("URL-Original2").build();
    VideoEntity v3=VideoEntity.builder()
            .id(3L).title("Video Admin3").duration(35L).videoUrl("URL-Original3").build();

when(repository.findAllById(videoIds)).thenReturn(List.of(v1, v2, v3));
when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://url-firmada.com");

List<VideoDto> results=service.videoInternalDetails(videoIds);

assertNotNull(results);
assertEquals("Video Admin1", results.get(0).getTitle());
assertEquals("http://url-firmada.com", results.get(0).getVideoUrl());
}

@Test
    @DisplayName("ADMIN: debe retornar 404 si el video no existe")
    void videoInternalDetails_ShouldThrowNotFound_WhenVideoNotExist(){
    UserPrincipal principal=new UserPrincipal(99L, "userAdmin");
    var auth=new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(repository.findAllById(List.of(88L))).thenReturn(Collections.emptyList());

    List<VideoDto> results=service.videoInternalDetails(List.of(88L));

    assertTrue(results.isEmpty());
}

@Test
@DisplayName("debe verificar el mensaje de error si el servicio de almacenamiento minio faya")
    void shouldThrowExceptionWhenMinioFails() throws Exception{
    when(minioClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("minio down"));

    assertThrows(RuntimeException.class, () -> service.generateUploadUrl("video.mp4"));
}

}
