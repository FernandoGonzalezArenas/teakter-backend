package com.teakter.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_id", columnList = "id"),
        @Index(name = "idx_notification_userId", columnList = "userId")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEntity {

@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false)
    private String userId; //id de el usuario que resibe la notificacion

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

@Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

private String targetUrl; //para redirigir al hacer click

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
