package com.teakter.notification.service;

import com.teakter.notification.dto.NotificationDto;
import com.teakter.notification.dto.UserPrincipal;
import com.teakter.notification.entity.NotificationEntity;
import com.teakter.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{

private final NotificationRepository notificationRepository;

@Override
    public List<NotificationDto> getUserNotifications(Long lastId, int limit){
    UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String userId=principal.userId();

    PageRequest pageRequest=PageRequest.of(0, limit);

List<NotificationEntity> notificacionesUsuario=notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, lastId, pageRequest);

if (notificacionesUsuario.isEmpty()) return Collections.emptyList();

return notificacionesUsuario.stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}

@Override
public Long getUnreadCount(){
    UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String userId=principal.userId();

    return notificationRepository.countByUserIdAndIsReadFalse(userId);
}

@Override
public void markAsRead(Long id){
    notificationRepository.findById(id).ifPresent(n -> {
        n.setIsRead(true);
        notificationRepository.save(n);
    });
}

private NotificationDto mapToDto(NotificationEntity notification){
    return NotificationDto.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .type(notification.getType().name())
            .isRead(notification.getIsRead())
            .targetUrl(notification.getTargetUrl())
            .build();
}

}
