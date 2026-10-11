package com.teakter.video.repository;

import com.teakter.video.entity.VideoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity, Long> {
    @Query("SELECT v FROM VideoEntity v WHERE v.status ='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY v.id DESC")
    Page<VideoEntity> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    //buscar shorts por titulo
@Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND v.duration < 60 ORDER BY v.id DESC")
    Page<VideoEntity> searchShortsByTitle(@Param("keyword") String keyword, Pageable pageable);

//buscar videos largos por titulo
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND v.duration >= 60 ORDER BY v.id DESC")
    Page<VideoEntity> searchLongVideosByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.duration < 60 AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC")
    List<VideoEntity> findNextShortVideos(@Param("lastId") Long lastId, Pageable pageable);

    //obtener el feed de videos largos
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.duration >= 60 AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC")
    List<VideoEntity> findNextLongVideos(@Param("lastId") Long lastId, Pageable pageable);

    //obtener informacion de los videos gustados por el usuario
@Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.id IN :ids AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC")
    List<VideoEntity> findByIdIn(@Param("ids") List<Long> ids, @Param("lastId") Long lastId, Pageable pageable);

//obtener informacion de los videos de los canales a los que esta subscrito el usuario
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.userId IN :channelsIds AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC")
    List<VideoEntity> findByUserIdInOrderByCreatedAtDesc(@Param("channelsIds") List<Long> channelsIds, @Param("lastId") Long lastId, Pageable pageable);

    //obtener los videos subidos por el usuario
    @Query("SELECT v FROM VideoEntity v WHERE v.status='READY' AND v.userId = :userId AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC")
    List<VideoEntity> findByUserId(@Param("userId") Long userId, @Param("lastId") Long lastId, Pageable pageable);
    List<VideoEntity> findAllByUserId(Long userId);
    void deleteByUserId(Long userId);

}
