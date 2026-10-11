package com.teakter.video.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "videos")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false)
private Long userId;

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
