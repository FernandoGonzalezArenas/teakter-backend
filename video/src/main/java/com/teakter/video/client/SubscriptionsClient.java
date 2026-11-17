package com.teakter.video.client;

import com.teakter.video.dto.IdsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "subscription", path = "/media/api/v1")
public interface SubscriptionsClient {

@GetMapping("/subs/user-subs/{userId}")
    IdsDto getSubscriptionsByUser(@PathVariable("userId") String userId);

}
