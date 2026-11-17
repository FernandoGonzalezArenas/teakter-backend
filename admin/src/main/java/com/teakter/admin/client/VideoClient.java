package com.teakter.admin.client;

import com.teakter.admin.dto.VideoMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "video", path = "/media/api/v1")
public interface VideoClient {

@DeleteMapping("/videos/internal/{id}")
    void deleteVideoInternal(@PathVariable("id") String id);

@GetMapping("/videos/internal/details")
List<VideoMetadataDto> getVideoDetails(@RequestParam("ids") List<String> videoIds);

}
