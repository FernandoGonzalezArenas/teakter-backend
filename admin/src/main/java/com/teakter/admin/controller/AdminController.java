package com.teakter.admin.controller;

import com.teakter.admin.dto.AuditorDto;
import com.teakter.admin.dto.ReportCreateDto;
import com.teakter.admin.dto.ReportDetailDto;
import com.teakter.admin.entity.PredefinedReason;
import com.teakter.admin.entity.ReportType;
import com.teakter.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "gestiona los reportes, baneos y eliminaciones de usuarios y contenido hecho por usuarios con privilejios de administrador")
public class AdminController {

private final AdminService adminService;

    @Operation(summary = "eliminar un video por contenido inapropiado",
            description = "se elimina el video que tiene reportes de contenido inapropiado y se verifica que los reportes son correctos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "video eliminado exitosamente"),
                    @ApiResponse(responseCode = "503", description = "no se pudo borrar el video, problema en el servicio de video")
                    })
@DeleteMapping("/videos/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteVideo(@PathVariable String videoId, @RequestBody AuditorDto datos){
    adminService.moderateVideo(videoId, datos.getReason());
    return ResponseEntity.ok("video eliminado por administracion");
}

    @Operation(summary = "eliminar una cuenta de usuario por contenido o comportamiento inapropiado",
            description = "se elimina la cuenta de el usuario reportado por contenido o comportamiento inapropiado, despues de verificar los reportes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "cuenta eliminada exitosamente"),
                    @ApiResponse(responseCode = "503", description = "no se pudo borrar la cuenta, problema con el servicio user")
                    })
@DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> banUser(@PathVariable String userId, @RequestBody AuditorDto datos){
    adminService.moderateUser(userId, datos.getReason());
    return ResponseEntity.ok("usuario baneado por administracion");
}

    @Operation(summary = "crear un reporte de video o de cuenta",
            description = "se crea un reporte para registrar algun contenido o comportamiento indevido y que sea revisado por algun administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "reporte creado exitosamente")
                    })
@PostMapping("/reports/{type}/{targetId}")
    public ResponseEntity<String> createReport(@PathVariable ReportType type,  @PathVariable String targetId, @RequestBody ReportCreateDto datos){
        try {
            PredefinedReason reason = PredefinedReason.valueOf(datos.getReason().toUpperCase());
            adminService.createReport(type, targetId, reason, datos.getDescription());
        } catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El motivo del reporte no es válido.");
        }
    return ResponseEntity.ok("reporte creado y enviado a revision");
}

    @Operation(summary = "obtener los reportes pendientes",
            description = "se obtienen los reportes pendientes para ser revisados y tomar alguna accion sobre ellos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "reportes pendientes obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ReportDetailDto.class)))
                    })
@GetMapping("/reports/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDetailDto>> getReports(){
    return ResponseEntity.ok(adminService.getPendingReports());
}

    @Operation(summary = "marcar un reporte como dismiss",
            description = "se marca el reporte como dismiss si no hay nada malo en el y se descarta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "reporte marcado con exito"),
                    @ApiResponse(responseCode = "404", description = "el reporte solicitado no existe")
                    })
@PatchMapping("/reports/{targetId}/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> dismiss(@PathVariable String targetId, @RequestParam("type") ReportType type){
    adminService.dismissReport(targetId, type);
    return ResponseEntity.ok("reporte descartado por el administrador");
}

}
