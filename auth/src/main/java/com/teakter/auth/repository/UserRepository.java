package com.teakter.auth.repository;

    import com.teakter.auth.entity.UserEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Modifying;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
        Optional<UserEntity> findByPublicUserId(String publicUserId);

    //obtener los usuarios expirados para eliminarlos con el ciclo de vida de JPA
    List<UserEntity> findByIsVerifiedFalseAndCreatedAtBefore(LocalDateTime limit);

@Modifying
        void deleteByIsVerifiedFalseAndCreatedAtBefore(LocalDateTime limit);
    }
