package com.teakter.admin.service;

import com.teakter.admin.client.UserClient;
import com.teakter.admin.client.VideoClient;
import com.teakter.admin.config.RabbitMQConfig;
import com.teakter.admin.dto.ReportDetailDto;
import com.teakter.admin.dto.UserPrincipal;
import com.teakter.admin.dto.VideoMetadataDto;
import com.teakter.admin.entity.*;
import com.teakter.admin.repository.AdminRepository;
import com.teakter.admin.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

@Mock
    private VideoClient videoClient;
@Mock
    private UserClient userClient;
@Mock
    private AdminRepository adminRepository;
@Mock
    private ReportRepository reportRepository;
@Mock
    private RabbitTemplate rabbitTemplate;

@InjectMocks
    private AdminServiceImpl adminService;

@BeforeEach
    void setup(){
    UserPrincipal principal=new UserPrincipal(1L, "adminUser");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    void moderateUser_ShouldBanUserAndNotify(){
    Long targetId=99L;
    String reason="spam detectado";

    when(reportRepository.findByTargetIdAndTypeAndStatus(targetId, ReportType.USER, ReportStatus.PENDING))
            .thenReturn(List.of());

    adminService.moderateUser(targetId, reason);

verify(userClient, times(1)).banUserInternal(targetId);
    ArgumentCaptor<AdminEntity> adminCaptor=ArgumentCaptor.forClass(AdminEntity.class);
    verify(adminRepository, times(1)).save(adminCaptor.capture());
    assertEquals("BAN_USER", adminCaptor.getValue().getActionType());
    assertEquals(targetId, adminCaptor.getValue().getTargetId());
    assertEquals(1L, adminCaptor.getValue().getAdminId());

    verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.ADMIN_EXCHANGE),
            eq(RabbitMQConfig.USER_BAN_RK),
            eq(targetId));
}

@Test
    void moderateVideo_ShouldDeleteVideoAndNotify(){
    Long videoId=500L;
    String reason="contenido inapropiado";

    when(reportRepository.findByTargetIdAndTypeAndStatus(videoId, ReportType.VIDEO, ReportStatus.PENDING))
            .thenReturn(List.of());

    adminService.moderateVideo(videoId, reason);

    verify(videoClient, times(1)).deleteVideoInternal(videoId);

    verify(adminRepository, times(1)).save(any(AdminEntity.class));
    verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.ADMIN_EXCHANGE),
            eq(RabbitMQConfig.VIDEO_DELETE_RK),
            eq(videoId));
}

@Test
    void shouldCreateReport(){
    ReportType type=ReportType.VIDEO;
    Long targetId=5L;
    PredefinedReason reason=PredefinedReason.VIOLENCIA;
    String description="el video muestra esenas explisitas";

    adminService.createReport(type, targetId, reason, description);

    ArgumentCaptor<ReportEntity> reportCaptor=ArgumentCaptor.forClass(ReportEntity.class);

    verify(reportRepository, times(1)).save(reportCaptor.capture());
    assertEquals(1L, reportCaptor.getValue().getReporterId());
    assertEquals(PredefinedReason.VIOLENCIA, reportCaptor.getValue().getReason());
    assertEquals(description, reportCaptor.getValue().getReportDescription());
}

@Test
    void shouldGetPendingReports(){
    VideoMetadataDto videoMetadataDto=VideoMetadataDto.builder()
            .id(20L)
                    .title("mi video")
                            .description("la descripcion")
                                    .videoUrl("url_del_video")
                                            .thumbnailUrl("thumbnail.jpg")
                                                    .build();
when(videoClient.getVideoDetails(anyList())).thenReturn(List.of(videoMetadataDto));

    ReportEntity r1=ReportEntity.builder()
            .targetId(20L)
            .type(ReportType.VIDEO)
            .reporterId(1L)
            .reason(PredefinedReason.VIOLENCIA)
            .status(ReportStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    ReportEntity r2=ReportEntity.builder()
            .targetId(30L)
            .type(ReportType.VIDEO)
            .reporterId(1L)
            .reason(PredefinedReason.SPAM)
            .status(ReportStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
when(reportRepository.findByStatus(ReportStatus.PENDING)).thenReturn(List.of(r1, r2));

List<ReportDetailDto> reports=adminService.getPendingReports();

assertEquals(2, reports.size());
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(20L)));
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(30L)));
}

@Test
    void shouldMarkDismissReport(){
    ReportEntity r1=ReportEntity.builder()
            .targetId(20L)
            .type(ReportType.VIDEO)
            .reporterId(1L)
            .reason(PredefinedReason.VIOLENCIA)
            .status(ReportStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    Long targetId=20L;
    ReportType type=ReportType.VIDEO;

when(reportRepository.findByTargetIdAndTypeAndStatus(targetId, type, ReportStatus.PENDING)).thenReturn(List.of(r1));

adminService.dismissReport(targetId, type);

assertEquals(ReportStatus.DISMISSED, r1.getStatus());
verify(reportRepository, times(1)).saveAll(anyList());
}

@Test
    void dismissReport_ShouldThrowNotFound_WhenIdDoesNotExist(){
    when(reportRepository.findByTargetIdAndTypeAndStatus(999L, ReportType.VIDEO, ReportStatus.PENDING)).thenReturn(List.of());

    assertThrows(ResponseStatusException.class, () ->{
        adminService.dismissReport(999L, ReportType.VIDEO);
    });
}

}
