package com.teakter.comments.repository;

import com.teakter.comments.entity.CommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByVideoId(String publicVideoId);
    Long countByVideoId(String publicVideoId);
    List<CommentEntity> findByUsernameAuthor(String usernameAuthor);
@Query("SELECT c FROM CommentEntity c WHERE c.videoId = :publicVideoId AND (:lastId IS NULL OR c.id < :lastId) ORDER BY c.id DESC")
    List<CommentEntity> findNextComments(String publicVideoId, Long lastId, Pageable pageable);

@Modifying
@Query("DELETE FROM CommentEntity c WHERE c.videoId = :publicVideoId")
void deleteByVideoId(@Param("publicVideoId") String publicVideoId);
void deleteByUserId(String userId);

}
