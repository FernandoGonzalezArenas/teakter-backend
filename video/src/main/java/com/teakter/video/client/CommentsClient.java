package com.teakter.video.client;

import com.teakter.video.dto.CommentsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "comments", path = "/media/api/v1")
public interface CommentsClient {

@GetMapping("/comentarios/{publicVideoId}")
    List<CommentsDto> getCommentsByVideo(@PathVariable("publicVideoId") String publicVideoId, @RequestParam(value = "lastId", required = false) Long lastId, @RequestParam(value = "limit", defaultValue = "10") Integer limit);

@GetMapping("/comentarios/{publicVideoId}/count")
    Long countCommentsInVideo(@PathVariable String publicVideoId);
}
