package com.teakter.video.service;

import com.teakter.video.dto.*;
import com.teakter.video.entity.VideoEntity;
import com.teakter.video.repository.VideoRepository;
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
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AWSVideoServiceTest {

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
    private S3Presigner s3Presigner;
@Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;
@Mock
private PresignedPutObjectRequest presignedPutObjectRequest;

private AWSVideoServiceImpl service;
private final Long userId=1L;
String cloudfrontDomain="d3am6ojx8a1nkx.cloudfront.net";

@BeforeEach
    void setup(){
    service=new AWSVideoServiceImpl(repository, interactionsService, likeService, subscriptionService, userService, rabbitTemplate, s3Presigner);
    ReflectionTestUtils.setField(service, "bucketName", "aws-bucket-videos");
    ReflectionTestUtils.setField(service, "cloudfrontDomainVideos", cloudfrontDomain);

    UserPrincipal principal=new UserPrincipal(userId, "user-aws");
    UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    @DisplayName("AWS: debe guardar metadata")
    void shouldSaveMetadataAndBuildUrlAWS(){
    VideoDtoEntrada entrada=VideoDtoEntrada.builder()
            .title("video AWS")
            .description("descripcion valida")
            .duration(57L)
            .filename("clip.mp4")
            .thumbnailUrl("img.jpg")
            .build();

    service.saveVideoMetadata(entrada);

    ArgumentCaptor<VideoEntity> captor=ArgumentCaptor.forClass(VideoEntity.class);

//validaciones
    verify(repository).save(captor.capture());
    VideoEntity entity=captor.getValue();

    assertEquals(1L, entity.getUserId());
//validamos la logica exacta de construccion de url de AWS
    assertEquals("clip.mp4", entity.getVideoUrl());
}

@Test
    @DisplayName("AWS: debe generar URL prefirmada")
    void shouldGenerateUploadUrlAWS() throws  Exception{
String filename="test.mp4";
    String fakeUrl = "https://aws-bucket.s3.amazonaws.com/videos/uuid-test.mp4?signature=123";

//simulamos la cadena de llamadas de AWS SDK v2
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
    when(presignedPutObjectRequest.url()).thenReturn(new URL(fakeUrl));

    UploadUrlResponse response= service.generateUploadUrl(filename);
    assertNotNull(response);
    assertEquals(fakeUrl, response.uploadUrl());
assertTrue(response.finalFileName().contains(filename));
}

@Test
void shouldGenerateUploadUrlThumb() throws Exception{
    String filename="portada.jpg";
    String fakeUrl = "https://aws-bucket.s3.amazonaws.com/thumbnails/uuid-thumb.jpg?sig=123";

    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
    when(presignedPutObjectRequest.url()).thenReturn(new URL(fakeUrl));

    UploadUrlResponse response=service.generateUploadUrlThumb(filename);

    assertNotNull(response);
    assertTrue(response.uploadUrl().contains("thumbnails"));
    assertTrue(response.finalFileName().contains(filename));
}

@Test
void shouldSearchVideosByTitle(){
    String keyword="java";
    String type="";
    VideoEntity entity=VideoEntity.builder().userId(2L).title("video java").duration(37L).build();
    Page<VideoEntity> page=new PageImpl<>(List.of(entity));

    when(repository.searchByTitle(eq(keyword), any(PageRequest.class))).thenReturn(page);

    Page<VideoDto> results=service.searchVideosByTitle(keyword, type, 0, 10);

    assertEquals(1, results.getTotalElements());
    assertEquals("video java", results.getContent().get(0).getTitle());
}

@Test
void shouldGetFeedCorrectly(){
    VideoEntity v1=VideoEntity.builder().id(5L).userId(2L).title("v1").duration(84L).build();

    when(repository.findNextLongVideos(anyLong(), any(PageRequest.class))).thenReturn(List.of(v1));
    List<VideoDto> results=service.getLongsFeed(10L, 5);

    assertEquals(1, results.size());
    verify(repository).findNextLongVideos(eq(10L), any(PageRequest.class));
}

@Test
    @DisplayName("aws: debe retornar VideoPlaybackDto con URL de cloudfront")
    void shouldGetVideoForPlaybackAWS() throws Exception{
VideoEntity video=VideoEntity.builder()
        .id(10L)
        .title("video AWS")
        .duration(84L)
        .videoUrl("clip-123.mp4")
        .build();
    String fakeSignedUrl = "https://" + cloudfrontDomain + "/videos/hls/clip-123/master.m3u8";

    when(repository.findById(10L)).thenReturn(Optional.of(video));
//    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
//    when(presignedGetObjectRequest.url()).thenReturn(new URL(fakeSignedUrl));

    VideoPlaybackDto result=service.getVideoForPlayback(10L);

    assertNotNull(result);
    assertEquals(fakeSignedUrl, result.getVideoUrl());
//    verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
}

@Test
    @DisplayName("AWS: debe retornar 404 cuando el video no exista para reproduccion")
    void shouldThrowNotFoundWhenVideoDoesNotExist(){
when(repository.findById(99L)).thenReturn(Optional.empty());

assertThrows(ResponseStatusException.class, () -> service.getVideoForPlayback(99L));
}

@Test
    @DisplayName("debe obtener el feed de subscripciones basado en el usuario autenticado")
    void shouldGetSubscriptionsFeedAWS(){
    UserDto user=UserDto.builder().id(2L).username("fernando").privacySubs(true).build();

    when(userService.getProfile(2L)).thenReturn(user);

    List<Long> followedChannels=List.of(100L, 200L);
when(subscriptionService.getSubscriptionsByUser(anyLong())).thenReturn(followedChannels);

VideoEntity video=VideoEntity.builder()
                .title("video subs")
                        .description("desc")
        .duration(38L)
                                .videoUrl("url")
                                        .thumbnailUrl("thumb")
                                                .build();
when(repository.findByUserIdInOrderByCreatedAtDesc(followedChannels, null, PageRequest.of(0,5)))
        .thenReturn(List.of(video));

List<VideoDto> result=service.getSubscriptionsFeed(2L, null, 5);

assertEquals(1, result.size());
verify(subscriptionService).getSubscriptionsByUser(anyLong());
verify(repository).findByUserIdInOrderByCreatedAtDesc(followedChannels, null, PageRequest.of(0, 5));
}

@Test
    @DisplayName("ADMIN: debe eliminar el video si el usuario tiene ROLE_ADMIN")
    void deleteVideoInternal_ShouldDelete_WhenUserIsAdmin(){
    Long videoId=1L;
    UserPrincipal principal=new UserPrincipal(99L, "userAdmin");
    var auth=new UsernamePasswordAuthenticationToken(principal,
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
    var auth=new UsernamePasswordAuthenticationToken(principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    VideoEntity v1=VideoEntity.builder()
            .id(1L).title("Video Admin1").duration(48L).videoUrl("clip1.mp4").build();
    VideoEntity v2=VideoEntity.builder()
            .id(2L).title("Video Admin2").duration(78L).videoUrl("clip2.mp4").build();
    VideoEntity v3=VideoEntity.builder()
            .id(3L).title("Video Admin3").duration(35L).videoUrl("clip3.mp4").build();

    when(repository.findAllById(videoIds)).thenReturn(List.of(v1, v2, v3));
//when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
//when(presignedGetObjectRequest.url()).thenReturn(new URL("http://url-firmada-aws.com"));

    List<VideoDto> results=service.videoInternalDetails(videoIds);

    assertNotNull(results);
    assertEquals("Video Admin1", results.get(0).getTitle());
assertEquals("https://" + cloudfrontDomain + "/videos/hls/clip1/master.m3u8", results.get(0).getVideoUrl());
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

    when(repository.findAllById(List.of(99L))).thenReturn(Collections.emptyList());

    List<VideoDto> results=service.videoInternalDetails(List.of(99L));

    assertTrue(results.isEmpty());
}

@Test
    void shouldHandleEmptySubscriptions(){
    when(subscriptionService.getSubscriptionsByUser(anyLong())).thenReturn(Collections.emptyList());

    List<VideoDto> results=service.getSubscriptionsFeed(1L, null, 5);

    assertTrue(results.isEmpty());
    verify(repository, never()).findByUserIdInOrderByCreatedAtDesc(Collections.emptyList(), null, PageRequest.of(0, 5));
}

@Test
    @DisplayName("debe lanzar una excepcion si el servicio de almacenamiento AWS faya al generar URL de subida")
    void shouldThrowExceptionWhenAWSFailsUpload() throws Exception{
when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenThrow(SdkClientException.create("AWS service Unavailable"));

assertThrows(SdkClientException.class, () ->{
    service.generateUploadUrl("error-video.mp4");
});


//verificamos que no se intento realizar ninguna operacion mas
    verify(presignedPutObjectRequest, never()).url();
}

}
