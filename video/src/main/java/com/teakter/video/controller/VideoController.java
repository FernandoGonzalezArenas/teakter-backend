package com.teakter.video.controller;

import com.teakter.video.dto.*;
import com.teakter.video.entity.VideoEntity;
import com.teakter.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video Controller", description = "gestiona la subida, visualizacion, y reproduccion de videos y miniaturas ademas de guardar la informacion de videos y miniaturas en base de datos")
public class VideoController {

private final VideoService videoService;

//controlador para generar una URL firmada para subir un video a S3
@Operation(summary = "obtener URL firmada para subir video a AWS o Minio",
description = "se obtiene la URL firmada para subir el videoo a cualquiera de los servicios de almacenamiento disponibles")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "solicitud exitosa y se obtiene el nombre unico de el archivo y la URL firmada de subida",
        content=@Content(schema = @Schema(implementation = UploadUrlResponse.class))),
    @ApiResponse(responseCode = "503", description = "error en el servidor al generar la URL")
})
    @GetMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(@RequestParam String filename){
       UploadUrlResponse  response= videoService.generateUploadUrl(filename);
    return ResponseEntity.ok(response);
    }

    //controlador para generar una URL firmada para subir una miniatura a S3 o minio
@Operation(summary = "obtener la URL firmada para subir la miniatura",
description = "obtener URL firmada para subir la miniatura de el video a cualquiera de los servicios de almacenamiento disponibles")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "solicitud exitosa y se obtiene el nombre unico de el archivo y la URL firmada",
        content = @Content(schema = @Schema(implementation = UploadUrlResponse.class))),
        @ApiResponse(responseCode = "503", description = "error de el servidor para la generacion de URL")
})
    @GetMapping("/upload-tu")
    public ResponseEntity<UploadUrlResponse> getUploadUrlThumb(@RequestParam String filename){
        UploadUrlResponse  response= videoService.generateUploadUrlThumb(filename);
        return ResponseEntity.ok(response);
    }

    //controlador para guardar los metadatos de el video despues de la subida
@Operation(summary = "guardar los datos de el video en la base de datos",
description = "se guardan los datos de el video con el nombre unico de el video y la miniatura para construir la URL despues con el servicio de almacenamiento deseado")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "datos de el video guardados con exito",
        content = @Content(schema = @Schema(implementation = VideoDto.class))),
        @ApiResponse(responseCode = "400", description = "datos de el video incompletos")
})
    @PostMapping("/save-metadata")
    public ResponseEntity<VideoDto> saveVideoMetadata(@Valid @RequestBody VideoDtoEntrada entrada){
    VideoDto saveVideo= videoService.saveVideoMetadata(entrada);
    return ResponseEntity.ok(saveVideo);
    }

    @Operation(summary = "buscar videos en el buscador",
    description = "se buscan los videos escribiendo lo que se quiere encontrar aplicando filtros como videos cortos, largos, o todos los resultados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "busqueda exitosa",
            content = @Content(schema = @Schema(implementation = VideoDto.class)))
    })
@GetMapping("/search")
public ResponseEntity<Page<VideoDto>> searchVideos(@RequestParam String keyword, @RequestParam(required = false) String type, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
    return ResponseEntity.ok(videoService.searchVideosByTitle(keyword, type, page, size));
}

@Operation(summary = "obtener los videos cortos en scrol infinito",
description = "se obtienen los videos de menos de 60 segundos de duracion en scrol infinito tomando como referencia el ultimo ID")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "videos obtenidos exitosamente",
        content = @Content(schema = @Schema(implementation = VideoDto.class)))
})
@GetMapping("/shorts-feed")
public ResponseEntity<List<VideoDto>> getShortsFeed(@RequestParam(required = false) Instant lastCreatedAt, @RequestParam(defaultValue = "10") int limit){
    return ResponseEntity.ok(videoService.getShortsFeed(lastCreatedAt, limit));
}

    @Operation(summary = "obtener los videos largos en scrol infinito",
            description = "se obtienen los videos con duracion mayor a 60 segundos en scrol infinito tomando como referencia el ultimo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "videos obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = VideoDto.class)))
    })
    @GetMapping("/long-feed")
    public ResponseEntity<List<VideoDto>> getLongsFeed(@RequestParam(required = false) Instant lastCreatedAt, @RequestParam(defaultValue = "10") int limit){
        return ResponseEntity.ok(videoService.getLongsFeed(lastCreatedAt, limit));
    }

    @Operation(summary = "obtener los comentarios y likes de el video",
description = "se obtienen los comentarios y likes de el video llamando a los microservicios correspondientes para que envien los datos de dicho video")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "datos obtenidos... posibilidad de datos parciales por faya de algun servicio",
        content = @Content(schema = @Schema(implementation = InteractionsDto.class))),
        @ApiResponse(responseCode = "404", description = "datos de el video no disponibles")
})
    @GetMapping("/interactions/{publicVideoId}")
    public ResponseEntity<InteractionsDto> getInteractions(@PathVariable String publicVideoId, @RequestParam(value = "lastId", required = false) Long lastId, @RequestParam(value = "limit", defaultValue = "10") Integer limit){
    return ResponseEntity.ok(videoService.getInteractions(publicVideoId, lastId, limit));
    }

    @Operation(summary = "obtener los datos de los videos gustados por el usuario",
            description = "se obtiene la informacion de los videos que le gustan al usuario y son pasados por parametro desde el frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "informacion obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = VideoDto.class)))
    })
    @GetMapping("/list-likes/{userId}")
    public ResponseEntity<List<VideoDto>> getVideosByIds(@PathVariable String userId, @RequestParam(required = false) Instant lastCreatedAt, @RequestParam(defaultValue = "10") int limit){
        return ResponseEntity.ok(videoService.getVideosByIds(userId, lastCreatedAt, limit));
    }

    @Operation(summary = "obtener la URL para reproducir el video",
    description = "se obtiene la URL para reproducir el video en el frontend desde el servicio de almacenamiento elegido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de reproduccion obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = VideoPlaybackDto.class))),
            @ApiResponse(responseCode = "404", description = "el video solicitado no fue encontrado"),
            @ApiResponse(responseCode = "503", description = "el servicio de almacenamiento no esta disponible")
    })
    @GetMapping("/{publicVideoId}/play")
    public ResponseEntity<VideoPlaybackDto> playVideo(@PathVariable String publicVideoId){
        return ResponseEntity.ok(videoService.getVideoForPlayback(publicVideoId));
    }

    @Operation(summary = "obtener los videos que ha subido el usuario",
    description = "se obtiene la informacion de los videos que ha subido el usuario para mostrarlos en la pagina de el frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "se obtiene exitosamente la informacion de los videos solicitados",
            content = @Content(schema = @Schema(implementation = VideoDto.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<VideoDto>> getVideosOfUser(@PathVariable String userId, @RequestParam(required = false) Instant lastCreatedAt, @RequestParam(defaultValue = "10") int limit){
    return ResponseEntity.ok(videoService.getVideosByUser(userId, lastCreatedAt, limit));
    }

    @Operation(summary = "obtener la informacion de los videos de los canales a los que el usuario esta subscrito",
    description = "se obtiene la informacion de los videos de los canales a los que esta subscrito el usuario llamando internamente a el microservicio de subscripciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "informacion de los videos obtenida correctamente",
            content = @Content(schema = @Schema(implementation = VideoDto.class)))
    })
    @GetMapping("/my-feed-subs/{userId}")
    public ResponseEntity<List<VideoDto>> getSubscriptionsFeed(@PathVariable String userId, @RequestParam(required = false) Instant lastCreatedAt, @RequestParam(defaultValue = "10") int limit){
        return ResponseEntity.ok(videoService.getSubscriptionsFeed(userId, lastCreatedAt, limit));
    }

    @Operation(summary = "eliminar un video desde administracion",
    description = "se elimina un video por contenido inapropiado desde administracion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "el video se elimino exitosamente"),
            @ApiResponse(responseCode = "403", description = "no se tienen los permisos necesarios para eliminar el video"),
            @ApiResponse(responseCode = "404", description = "el video que se quiere eliminar no existe")
    })
    @DeleteMapping("/internal/{publicVideoId}")
    public ResponseEntity<String> deleteVideoInternal(@PathVariable String publicVideoId){
        videoService.deleteVideoInternal(publicVideoId);
        return ResponseEntity.ok("video eliminado exitosamente por el administrador");
    }

    @Operation(summary = "obtener la informacion de el video solicitado por admin",
    description = "se obtienen los datos de el video que tiene algun reporte para ser revisado por algun administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "informacion del video obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = VideoDto.class))),
                    @ApiResponse(responseCode = "403", description = "no tiene los permisos necesarios para solicitar la informacion de el video"),
                    @ApiResponse(responseCode = "404", description = "el video solicitado no existe")
    })
    @GetMapping("/internal/details")
    public ResponseEntity<List<VideoDto>> getVideoDetails(@RequestParam("ids") List<String> ids){
List<VideoDto> videos = videoService.videoInternalDetails(ids);
return ResponseEntity.ok(videos);
    }

}
