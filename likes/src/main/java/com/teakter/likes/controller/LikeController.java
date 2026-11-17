package com.teakter.likes.controller;

import com.teakter.likes.Service.LikeService;
import com.teakter.likes.dto.IdsDto;
import com.teakter.likes.dto.VideoLikeStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Tag(name = "Like Controller", description = "gestiona todo lo que tiene que ver con likes como la accion de dar o quitar el like, contarlos, su status y los videos gustados por un usuario especifico")
public class LikeController {

private final LikeService likeService;

    @Operation(summary = "dar o quitar like",
            description = "se da o se quita el like segun el estado actual, si esta presionado se quita y viseversa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "like agregado"),
                    @ApiResponse(responseCode = "204", description = "like eliminado"),
                    @ApiResponse(responseCode = "400", description = "solicitud invalida, videoId nulo o invalido")
                    })
@PostMapping("/{videoId}/toggle")
    public ResponseEntity<Void> toggleLike(@PathVariable String videoId){
    boolean creado=likeService.toggleLike(videoId);
    if (creado){
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }else {
        return ResponseEntity.noContent().build();
    }
}

    @Operation(summary = "obtener el numero de likes de un video",
            description = "se obtiene el numero de likes de un video para mostrarlo como informacion en la pantalla")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "numero de likes obtenido exitosamente"),
                    @ApiResponse(responseCode = "404", description = "el video solicitado no existe")
                    })
@GetMapping("/{videoId}/count")
    public ResponseEntity<Long> countLikes(@PathVariable String videoId){
    return ResponseEntity.ok(likeService.countLikes(videoId));
}

    @Operation(summary = "obtener el status de los likes de un video",
            description = "se obtiene el estatus de los likes de un video como el numero y si esta presionado o no")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "estatus de likes obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = VideoLikeStatusDto.class))),
                    @ApiResponse(responseCode = "404", description = "el video solicitado no existe")
                    })
@GetMapping("/{videoId}/status")
    public ResponseEntity<VideoLikeStatusDto> getLikeStatus(@PathVariable String videoId){
    return ResponseEntity.ok(likeService.getVideoLikeStatus(videoId));
}

    @Operation(summary = "obtener los ID's de los videos gustados por el usuario",
            description = "se obtienen los ID's de los videos gustados por el usuario para despues pasarlos a el microservicio video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID's obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = IdsDto.class)))
                    })
@GetMapping("/user-likes/{userId}")
    public ResponseEntity<IdsDto> getLikesVideosByUserId(@PathVariable String userId){
    IdsDto videoIds=likeService.getLikesVideosByUserId(userId);
  return ResponseEntity.ok(videoIds);
}

}
