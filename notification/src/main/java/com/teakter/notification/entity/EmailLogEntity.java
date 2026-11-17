package com.teakter.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_emailLog_id", columnList = "id")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailLogEntity {

@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false)
    private String recipientEmail;

@Column(nullable = false)
    private String subject;

@Column(nullable = false)
    private String emailType; //Ej: "VERIFICATION", "PASSWORD_RESET", "WELCOME"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status; //SENT, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant sentAt;

}
