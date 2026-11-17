package com.teakter.notification.config;

import com.teakter.notification.dto.EmailVerificationRabbitDto;
import com.teakter.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

private final EmailService emailService;

@RabbitListener(queues = RabbitMQConfig.EMAIL_VERIFY_QUEUE)
    public void handleEmailVerificationEvent(EmailVerificationRabbitDto event){
    emailService.sendVerificationEmail(
            event.getEmail(),
            event.getUsername(),
            event.getVerificationToken()
    );
}

}
