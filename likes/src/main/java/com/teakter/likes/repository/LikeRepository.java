package com.teakter.likes.repository;

import com.teakter.likes.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
Optional<LikeEntity> findByUserIdAndVideoId(String userId, String videoId);
long countByVideoId(String videoId);
boolean existsByUserIdAndVideoId(String userId, String videoId);
List<LikeEntity> findByUserId(String userId);

@Modifying
@Query("DELETE FROM LikeEntity l WHERE l.videoId = :videoId")
void deleteByVideoId(@Param("videoId") String videoId);
void deleteByUserId(String userId);

}
