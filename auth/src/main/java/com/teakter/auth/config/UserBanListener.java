package com.teakter.auth.config;

import com.teakter.auth.repository.RefreshTokenRepository;
import com.teakter.auth.repository.UserRepository;
import com.teakter.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBanListener {

private final AuthService authService;

@RabbitListener(queues = "auth.user.ban.queue")
    public void handleUserBan(String userId){
    authService.banUser(userId);
}

}
