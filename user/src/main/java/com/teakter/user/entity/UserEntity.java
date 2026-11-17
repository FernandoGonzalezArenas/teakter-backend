package com.teakter.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles", indexes = {
        @Index(name = "idx_profile_id", columnList = "id")
})
@Setter
        @Getter
        @NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
@Id
    private Long id;

@Builder.Default
@Column(nullable = false, unique = true, updatable = false)
private String publicUserId= UUID.randomUUID().toString();

private String username;
private String bio;

@Builder.Default
private Boolean privacyLikes=false;

    @Builder.Default
    private  Boolean privacySubs=false;
private String profilePictureUrl;
private Instant createdAt;

}
