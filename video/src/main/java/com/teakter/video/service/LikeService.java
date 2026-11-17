package com.teakter.video.service;

import com.teakter.video.client.LikesClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

private final LikesClient likesClient;

@CircuitBreaker(name = "likes", fallbackMethod = "fallbackForLikedVideos")
    public List<String> getLikedVideos(String userId){
    return likesClient.likedVideos(userId).getIds();
}

public List<String> fallbackForLikedVideos(String userId, Throwable throwable){
    return Collections.emptyList();
}

}
