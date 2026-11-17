package com.teakter.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdsDto {

    @Schema(description = "lista de ID's de videos gustados por el usuario", example = "[1,5,7,28,36]")
private List<String> Ids;

}
