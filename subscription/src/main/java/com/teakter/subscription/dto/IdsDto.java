package com.teakter.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdsDto {

    @Schema(description = "lista de ID's de canales a los que esta subscrito un usuario", example = "[6,17,47,81,386]")
    private List<String> Ids;

}
