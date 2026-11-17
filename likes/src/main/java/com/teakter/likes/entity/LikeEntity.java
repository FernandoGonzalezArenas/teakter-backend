package com.teakter.likes.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "videoId"}), indexes = {
        @Index(name = "idx_like_id", columnList = "id"),
        @Index(name = "idx_like_videoId", columnList = "videoId"),
        @Index(name = "idx_like_userId", columnList = "userId")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeEntity {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(nullable = false)
    private String videoId;

@Column(nullable = false)
    private String userId;

}
