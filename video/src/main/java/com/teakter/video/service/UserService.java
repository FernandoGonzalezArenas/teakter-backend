package com.teakter.video.service;

import com.teakter.video.client.UserClient;
import com.teakter.video.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

private final UserClient userClient;

@CircuitBreaker(name = "user", fallbackMethod = "fallbackForProfile")
    public UserDto getProfile(String userId){
    return userClient.getProfile(userId);
}

public  UserDto fallbackForProfile(String userId, Throwable throwable){
    return UserDto.builder().id("").username("sistema").bio("usuario no disponible").build();
}

}
