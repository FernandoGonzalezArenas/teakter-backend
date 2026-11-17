package com.teakter.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsDto {

    @Schema(description = "ID unico de el comentario", example = "65")
    private  Long id;

    @Schema(description = "nombre unico de el usuario autor de el comentario", example = "fernando_dev")
private String usernameAuthor;

    @Schema(description = "contenido de el comentario", example = "este es mi comentario")
private String content;

    @Schema(description = "fecha de creacion de el comentario", example = "2026-04-04T11:04:37")
private String createdAt;

    @Schema(description = "fecha de actualizacion de el comentario si se edita", example = "2026-05-16T12:36:25")
private String updatedAt;
}
