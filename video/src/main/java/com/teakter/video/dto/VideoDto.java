package com.teakter.video.dto;

import com.teakter.video.entity.VideoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDto {

    @Schema(description = "identificador unico de el video", example = "27")
    private Long id;

@Schema(description = "ID de el usuario que subio el video", example = "24")
    private Long userId;

    @Schema(description = "titulo de el video", example = "mi video")
    private String title;

    @Schema(description = "descripcion que el dueño de el video quiera agregar", example = "este es mi video de prueba")
    private String description;

    @Schema(description = "duracion de el video en segundos", example = "247")
    private Long duration;

    @Schema(description = "nombre unico de el archivo de el video", example = "j3k45fsfjeuihjdgdk-video.mp4")
private String videoUrl;

    @Schema(description = "URL de la miniatura", example = "http://localhost:8080/bucket/thumbnails/shsihhj335hjhuhd-thumb.jpg")
    private String thumbnailUrl;

    private VideoStatus status;

    @Schema(description = "fecha de subida de el video", example = "2024-05-27T05:36:48")
    private Instant createdAt;
}
