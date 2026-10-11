package com.teakter.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsDto {

@Schema(description = "ID unico de el comentario", example = "65")
    private  Long id;

    @Schema(description = "ID de el video asociado a el comentario", example = "15")
    private Long videoId;

    @Schema(description = "ID unico de el usuario que hizo el comentario", example = "65")
    private Long userId;

    @Schema(description = "nombre unico de el usuario autor de el comentario", example = "fernando_dev")
    private String usernameAuthor;

    @Schema(description = "contenido de el comentario", example = "este es mi comentario")
    private String content;

    @Schema(description = "fecha de creacion de el comentario", example = "2025-07-27T12:37:46.358Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "fecha de actualizacion de el comentario si se edita", example = "2026-11-21T10:52:15.473Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant updatedAt;
}
