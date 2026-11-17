package com.teakter.video.service;

import com.teakter.video.config.RabbitMQConfig;
import com.teakter.video.dto.*;
import com.teakter.video.entity.VideoEntity;
import com.teakter.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract  class AbstractVideoService implements VideoService{

    protected final VideoRepository videoRepository;
protected final  InteractionsService interactionsService;
    protected final LikeService likeService;
protected final SubscriptionService subscriptionService;
protected final UserService userService;
protected  final RabbitTemplate rabbitTemplate;

    public abstract UploadUrlResponse generateUploadUrl(String filename);

    public abstract UploadUrlResponse generateUploadUrlThumb(String filename);

    //metodo para guardar los metadatos de el video
    @Override
    public VideoDto saveVideoMetadata(VideoDtoEntrada entrada){
UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String userId=principal.userId();

        //sanitisacion defenciba por si el nombre viene con espacios
String cleanFilename=sanitizeFilename(entrada.getFilename());
String cleanThumbnail=sanitizeFilename(entrada.getThumbnailUrl());

        VideoEntity video = new VideoEntity();
            video.setUserId(userId);
            video.setTitle(entrada.getTitle());
            video.setDescription(entrada.getDescription());
            video.setDuration(entrada.getDuration());
            video.setVideoUrl(cleanFilename);
            video.setThumbnailUrl(cleanThumbnail);
            videoRepository.save(video);

            VideoProcessEvent event=new VideoProcessEvent(
                    video.getId(),
                    cleanFilename
            );
rabbitTemplate.convertAndSend(
        RabbitMQConfig.VIDEO_EXCHANGE,
        RabbitMQConfig.VIDEO_PROCESS_HLS_RK,
        event
);
            return mapToDto(video);
        }

    @Override
    public Page<VideoDto> searchVideosByTitle(String keyword, String type, int page, int size){
        PageRequest pageable=PageRequest.of(page, size);
        Page<VideoEntity> result;

        if ("shorts".equalsIgnoreCase(type)){
            result = videoRepository.searchShortsByTitle(keyword, pageable);
        } else if ("videos".equalsIgnoreCase(type)) {
            result = videoRepository.searchLongVideosByTitle(keyword, pageable);
        } else {
            result = videoRepository.searchByTitle(keyword, pageable);
        }

return result.map(this::mapToDto);
    }

    @Override
    public List<VideoDto> getShortsFeed(Instant lastCreatedAt, int limit){
        PageRequest pageable=PageRequest.of(0, limit);
        List<VideoEntity> videos= videoRepository.findNextShortVideos(lastCreatedAt, pageable);
        return videos.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<VideoDto> getLongsFeed(Instant lastCreatedAt, int limit){
        PageRequest pageable=PageRequest.of(0, limit);
        List<VideoEntity> videos= videoRepository.findNextLongVideos(lastCreatedAt, pageable);

        if (videos.isEmpty())log.info("no hay videos para retornar");

        return videos.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public InteractionsDto getInteractions(String publicVideoId, Long lastId, Integer limit){

         List<CommentsDto> comments=interactionsService.getCommentsByVideo(publicVideoId, lastId, limit);
         Long totalComments = interactionsService.countCommentsInVideo(publicVideoId);
            LikeStatus status=interactionsService.likeStatusInVideo(publicVideoId);

        return new InteractionsDto(comments, totalComments, status);
    }

    @Override
    public List<VideoDto> getVideosByIds(String userId, Instant lastCreatedAt, int limit){
        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUser=principal.userId();

        if (!userId.equals(currentUser)) {
            UserDto perfil = userService.getProfile(userId);
            if (!perfil.getPrivacyLikes()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "acceso denegado, esta lista es privada");
            }
        }

        List<String> ids=likeService.getLikedVideos(userId);

        PageRequest pageRequest=PageRequest.of(0, limit);
        List<VideoEntity> results=videoRepository.findByIdIn(ids, lastCreatedAt, pageRequest);

        //convertimos la lista de entidades a lista de DTO correctamente y la retornamos
        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public VideoPlaybackDto getVideoForPlayback(String publicVideoId){
        //buscamos el video en la base de datos
        VideoEntity video=videoRepository.findByPublicVideoId(publicVideoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "video no encontrado"));

        //generamos la URL de acceso
        String urlFinal=getPlaybackUrl(video.getVideoUrl());
String thumbUrl=buildFullThumbnailUrl(video.getThumbnailUrl());

        //retornamos el DTO
        return VideoPlaybackDto.builder()
                .id(video.getPublicVideoId())
                .userId(video.getUserId())
                .title(video.getTitle())
                .description(video.getDescription())
                .duration(video.getDuration())
                .videoUrl(urlFinal)
                .thumbnailUrl(thumbUrl)
                .status(video.getStatus().name())
                .createdAt(video.getCreatedAt() != null ? video.getCreatedAt().toString() : null)
                .build();
    }

    //metodo abstracto para construir la URL personalizada con cada servicio de almacenamiento
    protected abstract String getPlaybackUrl(String storedUrl);

    @Override
    public List<VideoDto> getSubscriptionsFeed(String userId, Instant lastCreatedAt, int limit){
        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUser=principal.userId();

        if (!userId.equals(currentUser)) {
            UserDto perfil = userService.getProfile(userId);
            if (!perfil.getPrivacySubs()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "acceso denegado, lista privada");
            }
        }

List<String> followedIds=subscriptionService.getSubscriptionsByUser(userId);
if (followedIds.isEmpty()) return Collections.emptyList();

PageRequest pageRequest = PageRequest.of(0, limit);
List<VideoEntity> result=videoRepository.findByUserIdInOrderByCreatedAtDesc(followedIds, lastCreatedAt, pageRequest);

return result.stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
    }

    //obtener los videos subidos por un usuario especifico
    @Override
    public List<VideoDto> getVideosByUser(String userId, Instant lastCreatedAt, int limit){
        PageRequest pageRequest=PageRequest.of(0, limit);
        List<VideoEntity> videosUsuario = videoRepository.findByUserId(userId, lastCreatedAt, pageRequest);

        if (videosUsuario.isEmpty()) return Collections.emptyList();

        return videosUsuario.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

@Override
@Transactional
public void deleteVideoInternal(String publicVideoId){
    Authentication auth=SecurityContextHolder.getContext().getAuthentication();

    boolean isAdmin=auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
if (!isAdmin){
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "acceso denegado, no tienes derechos de administrador para borrar el video");
}

VideoEntity video=videoRepository.findByPublicVideoId(publicVideoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El video que se quiere eliminar no existe"));

videoRepository.deleteById(video.getId());
    }

    @Override
    @Transactional
    public void processUserBannedInternal(String userId){
//buscar los videos de el usuario
        List<VideoEntity> userVideos=videoRepository.findAllByUserId(userId);

        if (!userVideos.isEmpty()){
            //extraer los ids de los videos
            List<String> videoIds=userVideos.stream().map(VideoEntity::getPublicVideoId).toList();

//borrar los videos de el usuario
            videoRepository.deleteByUserId(userId);

            //mandar los ids de videos al exchange para borrar likes y comentarios de cada video
            for (String videoId : videoIds){
rabbitTemplate.convertAndSend(
        "admin.exchange",
        "video.deleted",
        videoId
);
            }
        }
    }

    @Override
    public List<VideoDto> videoInternalDetails(List<String> ids){
Authentication auth=SecurityContextHolder.getContext().getAuthentication();

boolean isAdmin=auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
if (!isAdmin){
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "acceso denegado, se requieren permisos de administrador para borrar el video");
}

        List<VideoEntity> videos=videoRepository.findAllByPublicVideoId(ids);

return videos.stream().map(video -> {
    VideoDto dto = mapToDto(video);

    String urlReproduccion = getPlaybackUrl(video.getVideoUrl());

    dto.setVideoUrl(urlReproduccion);
    return dto;
}).collect(Collectors.toList());
    }

    protected VideoDto mapToDto(VideoEntity video){
        String thumbUrl=buildFullThumbnailUrl(video.getThumbnailUrl());
return VideoDto.builder()
        .id(video.getPublicVideoId())
        .userId(video.getUserId())
        .title(video.getTitle())
        .description(video.getDescription())
        .duration(video.getDuration())
        .videoUrl(video.getVideoUrl())
        .thumbnailUrl(thumbUrl)
        .status(video.getStatus().name())
        .createdAt(video.getCreatedAt() != null ? video.getCreatedAt().toString() : null)
        .build();
    }

    protected String sanitizeFilename(String filename){
        if (filename == null) return null;

        //remuebe espacios multiples, tabulaciones y saltos de linea remplazandolos por "_" ademas de remover caracteres especiales
int lastDot=filename.lastIndexOf('.');
String name=(lastDot != -1) ? filename.substring(0, lastDot) : filename;
String extencion=(lastDot != -1) ? filename.substring(lastDot) : "";

String cleanName= name.replaceAll("\\s+", "_");
cleanName=cleanName.replaceAll("[^a-zA-Z0-9_-]", "");
String cleanExtencion=extencion.replaceAll("[^a-zA-Z0-9.]", "").toLowerCase();

return cleanName + cleanExtencion;
    }

    protected abstract String buildFullVideoUrl(String filename);

    protected abstract String buildFullThumbnailUrl(String filename);

}
