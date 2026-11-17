package com.teakter.subscription.config;

import com.teakter.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerRabbitListener {

    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = "user.subs.delete.queue")
    public void deleteSubscriptionsOfUser(String userId){
        //llamamos a el metodo para borrar las subscripciones
subscriptionService.deleteSubscriptionsOfUser(userId);
    }


}
