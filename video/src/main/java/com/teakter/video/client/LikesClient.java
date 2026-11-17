package com.teakter.video.client;

import com.teakter.video.dto.IdsDto;
import com.teakter.video.dto.LikeStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "likes", path = "/media/api/v1")
public interface LikesClient {

    @GetMapping("/like/{publicVideoId}/status")
    LikeStatus likeStatusInVideo(@PathVariable("publicVideoId") String publicVideoId);

    @GetMapping("/like/user-likes/{userId}")
    IdsDto likedVideos(@PathVariable("userId") String userId);
}
