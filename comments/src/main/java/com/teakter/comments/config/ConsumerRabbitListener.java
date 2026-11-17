package com.teakter.comments.config;

import com.teakter.comments.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerRabbitListener {

    private final CommentService commentService;

@RabbitListener(queues = "delete.user.comments.queue")
    public void consumeDeleteCommentsWithVideoId(String videoId){
    commentService.deleteCommentsWithVideoId(videoId);
}

@RabbitListener(queues = "user.ban.comments.queue")
    public void consumerCommentsUserBanned(String userId){
    commentService.deleteCommentsByUserId(userId);
}

}
