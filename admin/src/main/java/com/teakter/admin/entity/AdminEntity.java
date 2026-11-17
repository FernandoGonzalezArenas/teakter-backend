package com.teakter.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin-actions", indexes = {
        @Index(name = "idx_admin_id", columnList = "id"),
        @Index(name = "idx_admin_adminId", columnList = "adminId"),
        @Index(name = "idx_admin_targetId", columnList = "targetId")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminEntity {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

private String adminId; //el id de el administrador que viene de el gateway
    private String actionType; //accion realizada como DELETE_VIDEO o BAN_USER
    private String targetId; //el id de el recurso afectado
    private String reason; //justificacion de la accion realizada
    private Instant timestamp; //fecha de la accion

    @PrePersist
    protected void onCreate(){
        timestamp=Instant.now();
    }

}
