package com.teakter.notification.controller;

import com.teakter.notification.dto.NotificationDto;
import com.teakter.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

@GetMapping("/user")
    public ResponseEntity<List<NotificationDto>> getUserNotification(@RequestParam(required = false) Long lastId, @RequestParam(defaultValue = "10") int limit){
    return ResponseEntity.ok(
notificationService.getUserNotifications(lastId, limit)    );
}

@GetMapping("/user/unread-count")
    public ResponseEntity<Long> getUnreadCount(){
    return ResponseEntity.ok(notificationService.getUnreadCount());
}

@PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id){
    notificationService.markAsRead(id);
    return ResponseEntity.noContent().build();
}

}
