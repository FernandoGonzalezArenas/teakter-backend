package com.teakter.video.client;

import com.teakter.video.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user")
public interface UserClient {

@GetMapping("/users/{id}")
    UserDto getProfile(@PathVariable("id") String id);

}
