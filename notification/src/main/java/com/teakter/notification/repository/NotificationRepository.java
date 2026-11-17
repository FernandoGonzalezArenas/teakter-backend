package com.teakter.notification.repository;

import com.teakter.notification.entity.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND (:lastId IS NULL OR n.id < :lastId) ORDER BY n.id DESC")
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, @Param("lastId") Long lastId, Pageable pageable);
Long countByUserIdAndIsReadFalse(String userId);
}
