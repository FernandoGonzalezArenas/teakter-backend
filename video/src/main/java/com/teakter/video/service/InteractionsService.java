package com.teakter.video.service;

import com.teakter.video.client.CommentsClient;
import com.teakter.video.client.LikesClient;
import com.teakter.video.client.SubscriptionsClient;
import com.teakter.video.dto.CommentsDto;
import com.teakter.video.dto.LikeStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionsService {

private  final CommentsClient commentsClient;
private final LikesClient likesClient;
private final SubscriptionsClient subscriptionsClient;

    //metodos circuitbreaker para controlar las fayas de microservicios a los que se llama
    @CircuitBreaker(name = "comments", fallbackMethod = "fallbackForComments")
    public List<CommentsDto> getCommentsByVideo(String publicVideoId, Long lastId, Integer limit){
        return commentsClient.getCommentsByVideo(publicVideoId, lastId, limit);
    }

    public List<CommentsDto> fallbackForComments(String publicVideoId, Long lastId, Integer limit, Throwable throwable){
        return List.of(CommentsDto.builder().usernameAuthor("sistema").content("comentarios temporalmente no disponibles... ").build());
    }

    @CircuitBreaker(name = "comments", fallbackMethod = "fallbackCountComments")
    public Long countCommentsInVideo(String publicVideoId){
        return commentsClient.countCommentsInVideo(publicVideoId);
    }

    public Long fallbackCountComments(String publicVideoId, Throwable throwable){
        return 0L;
    }

    @CircuitBreaker(name = "likes", fallbackMethod = "fallbackForLikes")
    public LikeStatus likeStatusInVideo(String publicVideoId){
        return likesClient.likeStatusInVideo(publicVideoId);
    }

    public LikeStatus fallbackForLikes(String publicVideoId, Throwable throwable){
        return new LikeStatus(0L, false);
    }

    }
