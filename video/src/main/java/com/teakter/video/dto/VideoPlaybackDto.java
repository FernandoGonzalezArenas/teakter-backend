package com.teakter.video.dto;

import com.teakter.video.entity.VideoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoPlaybackDto {


    @Schema(description = "identificador de el video seleccionado", example = "26")
private Long id;

    @Schema(description = "ID de el usuario que subio el video", example = "24")
    private Long userId;

    @Schema(description = "titulo de el video seleccionado", example = "mi video")
private String title;

    @Schema(description = "descripcion de el video seleccionado", example = "este es mi video de prueba")
private String description;

    @Schema(description = "duracion de el video en segundos", example = "247")
    private Long duration;

    @Schema(description = "URL de reproduccion de el video seleccionado", example = "http://minio:9000/bucket/videos/7sghrukhdj-video.mp4?firma=firma")
private String videoUrl;

    @Schema(description = "URL de la miniatura", example = "http://localhost:8080/bucket/thumbnails/s7ghhghjd5-thumb.jpg")
private String thumbnailUrl;

    private VideoStatus status;

    @Schema(description = "fecha de subida de el video", example = "2024-05-27T05:36:48")
    private Instant createdAt;
}
