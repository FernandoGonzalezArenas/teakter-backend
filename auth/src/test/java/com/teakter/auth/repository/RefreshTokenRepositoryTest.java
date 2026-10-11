package com.teakter.auth.repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.teakter.auth.entity.RefreshTokenEntity;
import com.teakter.auth.entity.UserEntity;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.ANY)
public class RefreshTokenRepositoryTest {

@Autowired RefreshTokenRepository refreshRepo;
@Autowired UserRepository userRepo;

@Test
void  findByToken_ok(){
    //creamos un usuario y registro en la entidad de refresh
    UserEntity u=userRepo.save(UserEntity.builder()
.username("fernando")
.password("x")
                    .role("ROLE_USER")
.email("fer@mail.com")
.build());
    RefreshTokenEntity r=RefreshTokenEntity.builder()
    .token("REF")
    .expiryDate(Instant.now().plusSeconds(60))
    .user(u)
    .build();
    refreshRepo.save(r);

    //se busca el token
    var  out=refreshRepo.findByToken("REF");

    //verificaciones
    assertThat(out).isPresent();
    assertThat(out.get().getUser().getUsername()).isEqualTo("fernando");
}

@Test
void  deleteAllByExpiryDateBefore_eliminaExpirados(){
    UserEntity u=userRepo.save(UserEntity.builder()
.username("bob")
.password("b")
                    .role("ROLE_USER")
.email("bob@mail.com")
.build());

    //token expirado y vijente
    refreshRepo.save(RefreshTokenEntity.builder()
.token("OLD")
.expiryDate(Instant.now().minusSeconds(5))
.user(u)
.build());
    refreshRepo.save(RefreshTokenEntity.builder()
.token("NEW")
.expiryDate(Instant.now().plusSeconds(60))
.user(u)
.build());

    //borramos los expirados
    refreshRepo.deleteAllByExpiryDateBefore(Instant.now());

    //comprobamos que OLD fue borrado y NEW aun existe
    assertThat(refreshRepo.findByToken("OLD")).isEmpty();
    assertThat(refreshRepo.findByToken("NEW")).isPresent();
}

@Test
    @DisplayName("borrar los tokens de un usuario baneado")
    void shouldDeleteTokensOfBannedUser(){
    UserEntity user=userRepo.save(UserEntity.builder()
            .username("fer").password("mi-password").role("USER").email("yo@email.com").build());
    Long userId=user.getId();

    RefreshTokenEntity token=refreshRepo.save(RefreshTokenEntity.builder()
            .token("ref")
            .user(user)
            .expiryDate(Instant.now()).build());
    Long id=token.getId();

    refreshRepo.deleteByUserId(userId);
    Optional<RefreshTokenEntity> deleted=refreshRepo.findById(id);

assertTrue(deleted.isEmpty());
}

}
