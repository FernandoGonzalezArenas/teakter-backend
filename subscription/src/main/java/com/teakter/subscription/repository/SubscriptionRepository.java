package com.teakter.subscription.repository;

import com.teakter.subscription.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findBySubscriberIdAndChannelId(String subscriberId, String channelId);
    Long countByChannelId(String channelId);
    boolean existsBySubscriberIdAndChannelId(String subscriberId, String channelId);
    @Query("SELECT s.channelId FROM SubscriptionEntity s WHERE s.subscriberId = :subscriberId")
    List<String> findBySubscriberId(String subscriberId);
    @Modifying
    @Query("DELETE FROM SubscriptionEntity s WHERE s.channelId = :userId")
    void deleteSubscriptorsOfChannelId(String userId);

    @Modifying
    @Query("DELETE FROM SubscriptionEntity s WHERE s.subscriberId = :userId")
    void deleteSubscriptionsByChannelId(String userId);
}
