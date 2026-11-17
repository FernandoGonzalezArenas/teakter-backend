package com.teakter.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDtoEntrada {

    @Schema(description = "ID de el video asociado a el comentario", example = "17")
    @NotNull(message = "el videoId es obligatorio para guardar un comentario correctamente")
    private String videoId;

    @Schema(description = "contenido de el comentario", example = "este es mi comentario")
    @NotBlank(message = "debes ingresar el contenido de el mensaje")
    @Size(min = 1, max = 1200)
    private String content;

}
