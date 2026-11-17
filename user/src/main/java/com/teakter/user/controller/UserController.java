package com.teakter.user.controller;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserDto;
import com.teakter.user.dto.UserDtoEntrada;
import com.teakter.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "gestiona lo que tiene que ver con usuarios, como ver perfiles, actualizarlos, crearlos y eliminarlos")
public class UserController {

    private final UserService service;

    @Operation(summary = "obtener el perfil de el usuario solicitado",
            description = "se obtiene el perfil de el usuario solicitado para mostrarlo en el frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "404", description = "el perfil solicitado no existe")
                    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getPublicProfile(@PathVariable String id){
        return ResponseEntity.ok(service.getProfile(id));
    }

    @Operation(summary = "obtener la URL firmada para subir la foto de perfil",
            description = "se obtiene la URL firmada para subir la foto de perfil a algun servicio de almacenamiento disponible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de subida para la foto de perfil obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UploadUrlResponse.class))),
                    @ApiResponse(responseCode = "500", description = "el servicio de almacenamiento para subir la foto no esta disponible")
                    })
    @GetMapping("/upload-ppu")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(@RequestParam String filename){
        UploadUrlResponse response=service.getUploadUrl(filename);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "actualizar los datos de el perfil",
            description = "se actualizan los datos de el perfil de usuario con la biografía y el nombre unico de foto de perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "datos de el usuario actualizados exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "el cuerpo de la solicitud no es correcto"),
                    @ApiResponse(responseCode = "404", description = "el perfil de usuario solicitado no existe")
                    })
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(@Valid @RequestBody UserDtoEntrada profile){
        return ResponseEntity.ok(service.updateProfile(profile));
    }

    @GetMapping("/details")
    public ResponseEntity<List<UserDto>> getProfilesDetailsBatch(@RequestParam("ids") List<String> userIds){
        return ResponseEntity.ok(service.getProfilesBatch(userIds));
    }

    @Operation(summary = "eliminar un perfil de usuario desde administracion",
            description = "se elimina el perfil de usuario solicitado por administracion, verificando que la solicitud si sea hecha por un administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "perfil de usuario eliminado exitosamente"),
                    @ApiResponse(responseCode = "403", description = "no tiene los permisos necesarios para solicitar la eliminacion de el perfil"),
                    @ApiResponse(responseCode = "404", description = "el perfil de usuario solicitado no existe")
                    })
    @DeleteMapping("/internal/{id}")
    public ResponseEntity<String> banUserInternal(@PathVariable String id){
        service.banUserInternal(id);
        return ResponseEntity.ok("la cuenta fue borrada correctamente por un administrador");
    }

}
