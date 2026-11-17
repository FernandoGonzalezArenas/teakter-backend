package com.teakter.subscription.controller;


import com.teakter.subscription.dto.IdsDto;
import com.teakter.subscription.dto.SubscriptionStatusDto;
import com.teakter.subscription.service.SubscriptionService;
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
@RequestMapping("/subs")
@RequiredArgsConstructor
@Tag(name = "Subscription Controller", description = "gestiona todo lo que tiene que ver con las subscripciones como la accion de subscribirse o desubscribirse, contar las subscripciones de un canal, el estatus y los canales a los que esta subscrito un usuario")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "agregar o quitar la subscripcion",
            description = "se agrega o se quita la subscripcion segun el estado actual, si esta subscrito se quita y si no se agrega")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "subscripcion agregada"),
                    @ApiResponse(responseCode = "204", description = "subscripcion eliminada"),
            @ApiResponse(responseCode = "400", description = "solicitud invalida, ID de el canal invalido o nulo")
                    })
    @PostMapping("/{channelId}/toggle")
    public ResponseEntity<Void> toggleSubscription(@PathVariable String channelId){
        boolean status=subscriptionService.toggleSubscription(channelId);
        if (status){
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }else {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "contar las subscripciones de un canal",
            description = "se obtiene el numero de subscripciones de un canal para mostrarlo en el frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "numero de subscripciones obtenido exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Id de el canal invalido o nulo")
                    })
    @GetMapping("/{channelId}/count")
    public ResponseEntity<Long> countSubscriptors(@PathVariable String channelId){
        return ResponseEntity.ok(subscriptionService.countSubscriptions(channelId));
    }

    @Operation(summary = "obtener el status de subscripcion de un canal",
            description = "se obtiene el estatus de subscripcion de un canal como el numero de subscriptores y si el usuario actual esta subcrito o no")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "estatus de subscripcion de el canal obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = SubscriptionStatusDto.class))),
                    @ApiResponse(responseCode = "404", description = "ID de el canal invalido o nulo")
                    })
    @GetMapping("/{channelId}/status")
    public ResponseEntity<SubscriptionStatusDto> getChannelStatus(@PathVariable String channelId){
        return ResponseEntity.ok(subscriptionService.getChannelStatus(channelId));
    }

    @Operation(summary = "obtener los ID's de canales a los que esta subscrito un usuario",
            description = "se obtienen los ID's de los canales a los que esta subscrito el usuario para pasarlos a el micro video y asi obtener la informacion de los videos de los canales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ID's de los canales obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = IdsDto.class)))
                    })
    @GetMapping("/user-subs/{userId}")
    public ResponseEntity<IdsDto> getSubscriptionsByUser(@PathVariable String userId){
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByUser(userId));
    }

}
