package com.teakter.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

private Long id;
private String userId;
private String title;
private String message;
private String type;
private Boolean isRead;
private String targetUrl;

}
