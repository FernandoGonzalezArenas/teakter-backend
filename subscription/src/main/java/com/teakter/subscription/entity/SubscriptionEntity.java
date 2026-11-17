package com.teakter.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "channel_id"}), indexes = {
        @Index(name = "idx_subscription_id", columnList = "id"),
        @Index(name = "idx_subscription_subscriberId", columnList = "subscriberId"),
        @Index(name = "idx_subscription_channelId", columnList = "channelId")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscriber_id")
    private String subscriberId;

    @Column(name = "channel_id")
    private String channelId;

}
