package com.teakter.video.service;

import com.teakter.video.client.SubscriptionsClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionsClient subscriptionsClient;

    @CircuitBreaker(name = "subscription", fallbackMethod = "fallbackForSubscription")
    public List<String> getSubscriptionsByUser(String userId) {
        return subscriptionsClient.getSubscriptionsByUser(userId).getIds();
    }

    public List<String> fallbackForSubscription(String userId, Throwable throwable) {
        return Collections.emptyList();
    }

}

