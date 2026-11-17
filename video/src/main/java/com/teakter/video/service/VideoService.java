package com.teakter.video.service;

import com.teakter.video.dto.*;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public interface VideoService {

    UploadUrlResponse generateUploadUrl(String filename);

    UploadUrlResponse generateUploadUrlThumb(String filename);

 VideoDto saveVideoMetadata(VideoDtoEntrada entrada);

Page<VideoDto> searchVideosByTitle(String keyword, String type, int page, int size);

List<VideoDto> getShortsFeed(Instant lastCreatedAt, int limit);

    List<VideoDto> getLongsFeed(Instant lastCreatedAt, int limit);

 InteractionsDto getInteractions(String publicVideoId, Long lastId, Integer limit);

 List<VideoDto> getVideosByIds(String userId, Instant lastCreatedAt, int limit);

 VideoPlaybackDto getVideoForPlayback(String publicVideoId);

 List<VideoDto> getSubscriptionsFeed(String userId, Instant lastCreatedAt, int limit);

 List<VideoDto> getVideosByUser(String userId, Instant lastCreatedAt, int limit);

 void deleteVideoInternal(String publicVideoId);

List<VideoDto> videoInternalDetails(List<String> ids);

void processUserBannedInternal(String userId);

}
