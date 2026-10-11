package com.teakter.auth.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.teakter.auth.entity.UserEntity;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest //arranca contexto minimo JPa + H2
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.ANY) // fuersa a H2
public class UserRepositoryTest {

@Autowired UserRepository repo;

@Test
void  findByUsername_devuelveUsuario(){
    //guardamos entidad
    UserEntity u=UserEntity.builder()
    .username("fernando")
    .password("{noop}123")
            .role("ROLE_USER")
    .email("fer@mail.com")
    .build();
    repo.save(u);

    //buscamos por ussername
    Optional<UserEntity> out=repo.findByUsername("fernando");

    //verificaciones
assertThat(out).isPresent();
assertThat(out.get().getEmail()).isEqualTo("fer@mail.com");
}

@Test
void  findByUsername_vacio(){
    assertThat(repo.findByUsername("ghost")).isEmpty();
}

@Test
    @DisplayName("debe borrar la cuenta de el usuario de la base de datos")
    void shouldDeleteUser(){
    UserEntity user=repo.save(UserEntity.builder()
            .username("fer").password("mi-contraseña").role("USER").email("yo@email.com").build());
    Long id=user.getId();

    repo.deleteById(id);
Optional<UserEntity> deleted=repo.findById(id);

assertTrue(deleted.isEmpty());
}

}
