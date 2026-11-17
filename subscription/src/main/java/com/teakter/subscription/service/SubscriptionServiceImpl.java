package com.teakter.subscription.service;

import com.teakter.subscription.dto.IdsDto;
import com.teakter.subscription.dto.SubscriptionStatusDto;
import com.teakter.subscription.dto.UserPrincipal;
import com.teakter.subscription.entity.SubscriptionEntity;
import com.teakter.subscription.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public boolean toggleSubscription(String channelId){
        if (channelId==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "problema con el ID de el canal");
        }

        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String subscriberId=principal.userId();

        Optional<SubscriptionEntity> existingSubscription=subscriptionRepository.findBySubscriberIdAndChannelId(subscriberId, channelId);
        if (existingSubscription.isPresent()){
            subscriptionRepository.delete(existingSubscription.get());
            return false;
        }else {
            try {
                SubscriptionEntity newSub = SubscriptionEntity.builder()
                        .subscriberId(subscriberId)
                        .channelId(channelId)
                        .build();
                subscriptionRepository.save(newSub);
                return true;
            }catch (DataIntegrityViolationException e){
                return true;
            }
        }
    }

    @Override
    public boolean hasUserSubscription(String channelId){
        if (channelId==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "problema con el ID de el canal");
        }
var auth=SecurityContextHolder.getContext().getAuthentication();

        if (auth==null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
            return false;
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String subscriberId = principal.userId();

        return subscriptionRepository.existsBySubscriberIdAndChannelId(subscriberId, channelId);
    }

    @Override
    public Long countSubscriptions(String channelId){
        if (channelId==null || channelId.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "problema con el ID de el canal");
        }
        return subscriptionRepository.countByChannelId(channelId);
    }

    @Override
    public SubscriptionStatusDto getChannelStatus(String channelId){
        boolean statusSubscription=hasUserSubscription(channelId);
        Long totalSubs=countSubscriptions(channelId);

        return SubscriptionStatusDto.builder()
                .subscriptionByUser(statusSubscription)
                .totalSubscriptions(totalSubs)
                .build();
    }

    @Override
    public IdsDto getSubscriptionsByUser(String userId){

        //se obtiene la lista de los canales a los que sigue el usuario
        List<String> ressult=subscriptionRepository.findBySubscriberId(userId);

        //retornamos los ids en el DTO
        return new IdsDto(ressult);
    }

    @Override
    @Transactional
    public void deleteSubscriptionsOfUser(String userId){
subscriptionRepository.deleteSubscriptorsOfChannelId(userId);

subscriptionRepository.deleteSubscriptionsByChannelId(userId);
    }

}
