package com.teakter.auth.config;

import com.teakter.auth.dto.UserRabbitDto;
import com.teakter.auth.entity.UserEntity;
import com.teakter.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializerConfig {

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private final RabbitTemplate rabbitTemplate;

@Bean
    public CommandLineRunner initializeAdmin(){
    return args -> {
        String adminUsername = "fernando";
        if (userRepository.findByUsername(adminUsername).isEmpty()){
            UserEntity admin = UserEntity.builder()
                    .publicUserId("sjklfije8eh8ihdukf")
                    .username(adminUsername)
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@teakter.com")
                    .role("ROLE_ADMIN")
                    .isVerified(true)
                    .address("Aguascalientes, MX")
                    .phone("4491234567")
                    .build();

            userRepository.save(admin);

            try {
                Thread.sleep(3000);

                Long userId = admin.getId();
                String publicUserId = admin.getPublicUserId();
                String username = admin.getUsername();
                UserRabbitDto message = new UserRabbitDto(userId, publicUserId, username);
                rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_CREATE_RK, message);
            } catch (Exception e){
                log.error("no se pudo conectar a rabbitMQ (posible entorno de pruebas): {}", e);
            }

        }
    };
}

}
