package com.teakter.likes.Service;

import com.teakter.likes.dto.IdsDto;
import com.teakter.likes.dto.VideoLikeStatusDto;

public interface LikeService {
    boolean toggleLike(String videoId);
    Boolean hasUserLiked(String videoId);
    long countLikes(String videoId);

     VideoLikeStatusDto getVideoLikeStatus(String videoId);

     IdsDto getLikesVideosByUserId(String userId);

void deleteLikesVideo(String videoId);

void deleteLikesByUserId(String userId);
}
