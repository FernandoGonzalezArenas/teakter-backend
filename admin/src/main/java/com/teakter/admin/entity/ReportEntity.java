package com.teakter.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_id", columnList = "id"),
        @Index(name = "idx_report_targetId", columnList = "targetId"),
        @Index(name = "idx_report_reporterId", columnList = "reporterId")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetId; //id de el recurso solicitado

@Enumerated(EnumType.STRING)
private ReportType type; //tipo de el recurso

    private String reporterId; //id de el usuario que reporta

    @Enumerated(EnumType.STRING)
    private PredefinedReason reason; //motivo

@Column(columnDefinition = "TEXT")
    private String reportDescription;

    @Builder.Default
@Enumerated(EnumType.STRING)
    private ReportStatus status=ReportStatus.PENDING;
private Instant createdAt;

@PrePersist
    protected void onCreate(){
    this.createdAt=Instant.now();
}
}
