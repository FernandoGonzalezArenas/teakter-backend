package com.teakter.likes.config;

import com.teakter.likes.Service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerRabbitListener {

private final LikeService likeService;

@RabbitListener(queues = "delete.video.likes.queue")
    public void consumerDeleteLikes(String videoId){
    likeService.deleteLikesVideo(videoId);
}

@RabbitListener(queues = "user.ban.likes.queue")
    public void consumerUserBanned(String userId){
    likeService.deleteLikesByUserId(userId);
}

}
