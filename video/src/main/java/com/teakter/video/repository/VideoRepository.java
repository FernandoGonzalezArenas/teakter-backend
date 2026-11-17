package com.teakter.video.repository;

import com.teakter.video.entity.VideoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<VideoEntity, Long> {
    Optional<VideoEntity> findByPublicVideoId(String publicVideoId);
    @Query("SELECT v FROM VideoEntity v WHERE v.status ='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY v.id DESC")
    Page<VideoEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    //buscar shorts por titulo
@Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND v.duration < 60 ORDER BY v.id DESC")
    Page<VideoEntity> searchShortsByTitle(@Param("keyword") String keyword, Pageable pageable);

//buscar videos largos por titulo
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND v.duration >= 60 ORDER BY v.id DESC")
    Page<VideoEntity> searchLongVideosByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.duration < 60 AND (cast(:lastCreatedAt as timestamp) IS NULL OR v.createdAt < :lastCreatedAt) ORDER BY v.createdAt DESC, v.id DESC")
    List<VideoEntity> findNextShortVideos(@Param("lastCreatedAt") Instant lastCreatedAt, Pageable pageable);

    //obtener el feed de videos largos
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.duration >= 60 AND (cast(:lastCreatedAt as timestamp) IS NULL OR v.createdAt < :lastCreatedAt) ORDER BY v.createdAt DESC, v.id DESC")
    List<VideoEntity> findNextLongVideos(@Param("lastCreatedAt") Instant lastCreatedAt, Pageable pageable);

    //obtener informacion de los videos gustados por el usuario
@Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.publicVideoId IN :ids AND (cast(:lastCreatedAt as timestamp) IS NULL OR v.createdAt < :lastCreatedAt) ORDER BY v.createdAt DESC, v.id DESC")
    List<VideoEntity> findByIdIn(@Param("ids") List<String> ids, @Param("lastCreatedAt") Instant lastCreatedAt, Pageable pageable);

//obtener informacion de los videos de los canales a los que esta subscrito el usuario
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.userId IN :channelsIds AND (cast(:lastCreatedAt as timestamp) IS NULL OR v.createdAt < :lastCreatedAt) ORDER BY v.createdAt DESC, v.id DESC")
    List<VideoEntity> findByUserIdInOrderByCreatedAtDesc(@Param("channelsIds") List<String> channelsIds, @Param("lastCreatedAt") Instant lastCreatedAt, Pageable pageable);

    //obtener los videos subidos por el usuario
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.userId = :userId AND (cast(:lastCreatedAt as timestamp) IS NULL OR v.createdAt < :lastCreatedAt) ORDER BY v.createdAt DESC, v.id DESC")
    List<VideoEntity> findByUserId(@Param("userId") String userId, @Param("lastCreatedAt") Instant lastCreatedAt, Pageable pageable);
    List<VideoEntity> findAllByUserId(String userId);
    void deleteByUserId(String userId);

    @Query("SELECT v FROM VideoEntity v WHERE v.publicVideoId IN :publicIds")
    List<VideoEntity> findAllByPublicVideoId(List<String> publicIds);

}
