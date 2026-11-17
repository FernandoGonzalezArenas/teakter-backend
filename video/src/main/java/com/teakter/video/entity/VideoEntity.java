package com.teakter.video.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_video_id", columnList = "id"),
        @Index(name = "idx_video_publicVideoId", columnList = "publicVideoId"),
        @Index(name = "idx_video_userId", columnList = "userId")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false, unique = true, updatable = false)
private String publicVideoId = UUID.randomUUID().toString();

@Column(nullable = false)
private String userId;

@Column(nullable = false)
    private String title;

@Column(nullable = false)
    private String description;

@Column(nullable = false)
private Long duration;

@Column(nullable = false)
    private String videoUrl;

private String thumbnailUrl;

@Builder.Default
@Enumerated(EnumType.STRING)
private VideoStatus status = VideoStatus.PROCESSING;

@Column(nullable = false)
    private Instant createdAt;

@PrePersist
    protected void onCreate(){
    createdAt=Instant.now();
}
}
