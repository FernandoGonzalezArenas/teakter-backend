package com.teakter.user.config;

import com.teakter.user.dto.UserRabbitDto;
import com.teakter.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserConsumer {

private final UserService userService;

@RabbitListener(queues = "user.profile.create.queue")
    public void consumeRegistrationMessage(UserRabbitDto message){
    Long id=message.getUserId();
    String publicUsserId=message.getPublicUserId();
    String username=message.getUsername();

    //llamamos a el metodo para crear el perfil
    userService.createInitialProfile(id, publicUsserId, username);
}

}
