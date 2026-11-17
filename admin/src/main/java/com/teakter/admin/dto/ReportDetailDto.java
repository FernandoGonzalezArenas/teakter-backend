package com.teakter.admin.dto;

import com.teakter.admin.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetailDto {

    @Schema(description = "tipo de reporte", example = "VIDEO", allowableValues = {"VIDEO", "USER"})
private ReportType type;

    @Schema(description = "ID de el recurso reportado (video o usuario)", example = "23")
    private String targetId;

    private int totalReports;

    @Schema(description = "motivo por el que se reporta el recurso", example = "violencia")
    private Map<String, Long> reasonsCount;

    private List<IndividualReportDto> reportDescriptions;

    @Schema(description = "ID de el usuario que subio el video", example = "24")
    private String videoUserId;

    @Schema(description = "titulo de el video si el reportado es un video", example = "mi video")
    private String videoTitle;

    @Schema(description = "descripcion de el video reportado", example = "esta es la descripcion de el video reportado")
private String videoDescription;

    @Schema(description = "duracion de el video en segundos", example = "247")
    private Long videoDuration;

    @Schema(description = "URL de reproduccion de el video", example = "http://minio:9000/bucket/videos/ashue-video.mp4")
private String videoUrl;

    @Schema(description = "URL de la miniatura de el video reportado", example = "http://minio:9000/bucket/thumbnails/ahduiyehjshfuue-thumb.jpg")
private String thumbnailUrl;

    private String videoStatus;

    @Schema(description = "fecha de subida de el video", example = "2024-05-27T05:36:48")
    private String videoCreatedAt;

    @Schema(description = "nombre de usuario si la reportada es una cuenta de usuario", example = "fernando_dev")
private String targetUsername;

    @Schema(description = "URL de la foto de perfil de el usuario", example = "http://minio:9000/bucket/profiles/fsjhi8ehd-perfil.jpg")
    private String profilePictureUrl;

    @Schema(description = "estatus de el reporte", example = "PENDING", allowableValues = {"PENDING", "DISMISS", "RESOLVED"})
private String status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndividualReportDto {

        @Schema(description = "ID de el reporte", example = "12")
        private Long reportId; //id de el reporte

        private String reason;
        private String description;

        @Schema(description = "fecha de creacion de el reporte", example = "2024-05-27T11:14:07")
        private String createdAt;

    }


}
