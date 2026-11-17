package com.teakter.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    @Schema(description = "ID unico de el usuario", example = "76")
    private String id;

    @Schema(description = "nombre unico de usuario", example = "fernando_dev")
private String username;

    @Schema(description = "descripcion de el perfil de el usuario", example = "soy fernando_dev y este es mi perfil")
private String bio;

    @Schema(description = "URL de la foto de perfil de el usuario", example = "http://minio:9000/bucket/profiles/fsjhi8ehd-perfil.jpg")
private String profilePictureUrl;

    @Schema(description = "fecha de creacion de el perfil", example = "2024-04-17T09                                                                            :                                                                               23:46")
private String createdAt;

}
