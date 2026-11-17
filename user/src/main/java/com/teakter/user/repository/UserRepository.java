package com.teakter.user.repository;

import com.teakter.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByPublicUserId(String publicUserId);

    @Query("SELECT u FROM UserEntity u WHERE u.publicUserId in :userIds")
    List<UserEntity> findAllByPublicUserId(List<String> userIds);
}
