package com.teakter.comments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_id", columnList = "id"),
        @Index(name = "idx_comments_videoId", columnList = "videoId"),
        @Index(name = "idx_comments_userId", columnList = "userId")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false)
    private String videoId;

@Column(nullable = false)
private String userId;

@Column(nullable = false)
    private String usernameAuthor;

@Column(nullable = false, length = 1200)
    private String content;

@Column(nullable = false, updatable = false)
    private Instant createdAt;

@Column(nullable = false)
    private Instant updatedAt;

@PrePersist
    protected void  onCreate(){
createdAt=updatedAt=Instant.now();
}

@PreUpdate
    protected void  onUpdate(){
    updatedAt=Instant.now();
}

}
