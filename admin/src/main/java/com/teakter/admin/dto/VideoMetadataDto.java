package com.teakter.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoMetadataDto {

    @Schema(description = "identificador unico de el video reportado", example = "26")
    private String id;

    @Schema(description = "ID de el usuario que subio el video", example = "24")
    private String userId;

    @Schema(description = "titulo de el video reportado", example = "mi video")
private String title;

    @Schema(description = "descripcion de el video reportado", example = "esta es la descripcion de mi video")
private String description;

    @Schema(description = "duracion de el video en segundos", example = "247")
    private Long duration;

    @Schema(description = "URL de el video reportado para reproducirlo", example = "http://minio:9000/bucket/videos/hausyure84yeydfh-video.mp4")
private String videoUrl;

    @Schema(description = "URL de la miniatura de el video reportado", example = "http://minio:9000/buket/thumbnails/6sgfhee44dhjdhgdujhfd-thumb.jpg")
private String thumbnailUrl;

    private String videoStatus;

    @Schema(description = "fecha de subida de el video", example = "2024-05-27T05:36:48")
    private String createdAt;
}
