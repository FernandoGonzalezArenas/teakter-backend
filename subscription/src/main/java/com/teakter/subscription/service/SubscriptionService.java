package com.teakter.subscription.service;


import com.teakter.subscription.dto.IdsDto;
import com.teakter.subscription.dto.SubscriptionStatusDto;

public interface SubscriptionService {

    boolean toggleSubscription(String channelId);
    boolean hasUserSubscription(String channelId);
    Long countSubscriptions(String channelId);
    SubscriptionStatusDto getChannelStatus(String channelId);
    IdsDto getSubscriptionsByUser(String userId);
    void deleteSubscriptionsOfUser(String userId);

}
