package com.teakter.user.repository;

import com.teakter.user.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

@Autowired
    private UserRepository userRepository;

@Test
    @DisplayName("debe borrar un usuario fisicamente de la base de datos")
    void shouldDeleteUserFromDatabase(){
    UserEntity user=userRepository.save(UserEntity.builder()
            .id(1L).username("fernando").bio("biografía").profilePictureUrl("prof").privacyLikes(true).privacySubs(false).build());
    Long id=user.getId();

    userRepository.deleteById(id);
    Optional<UserEntity> deleted=userRepository.findById(id);

assertTrue(deleted.isEmpty());
}

}
