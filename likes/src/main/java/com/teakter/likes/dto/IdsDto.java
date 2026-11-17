package com.teakter.likes.dto;

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

    @Schema(description = "lista de videos gustados por el usuario", example = "[3,7,16,26,29,32]")
private List<String> Ids;

}
