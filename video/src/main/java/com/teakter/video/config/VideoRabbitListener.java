package com.teakter.video.config;

import com.teakter.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoRabbitListener {

private final VideoService videoService;

@RabbitListener(queues = "user.ban.video.queue")
    public void handleUserBanned(String userId){
    log.info("dentro de el listener para elimminar los videos de el usuario baneado identificado con el id = {}", userId);
    videoService.processUserBannedInternal(userId);
}

}
