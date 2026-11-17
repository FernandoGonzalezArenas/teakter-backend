package com.teakter.admin.service;

import com.teakter.admin.client.UserClient;
import com.teakter.admin.client.VideoClient;
import com.teakter.admin.config.RabbitMQConfig;
import com.teakter.admin.dto.ReportDetailDto;
import com.teakter.admin.dto.UserPrincipal;
import com.teakter.admin.dto.UserProfileDto;
import com.teakter.admin.dto.VideoMetadataDto;
import com.teakter.admin.entity.*;
import com.teakter.admin.repository.AdminRepository;
import com.teakter.admin.repository.ReportRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final VideoClient videoClient;
    private final UserClient userClient;
    private final AdminRepository adminRepository;
    private final ReportRepository reportRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    @CircuitBreaker(name = "video", fallbackMethod = "fallbackModerateVideo")
    public void moderateVideo(String videoId, String reason){
//obtener el id de el administrador guardado en el contexto desde el gateway
        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminId=principal.userId();

        //borrar el video fisicamente de forma sincrona con feignClient
        videoClient.deleteVideoInternal(videoId);

        //auditoría: registrar en la base de datos quien realiza la accion de borrado
        AdminEntity action=AdminEntity.builder()
                .adminId(adminId)
                .targetId(videoId)
                .reason(reason)
                .actionType("DELETE_VIDEO")
                .build();
        adminRepository.save(action);

        //accion asincrona, notificar eliminacion de comentarios y likes de el video eliminado
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ADMIN_EXCHANGE, RabbitMQConfig.VIDEO_DELETE_RK,
                videoId);

        List<ReportEntity> activeReports=reportRepository.findByTargetIdAndTypeAndStatus(videoId, ReportType.VIDEO, ReportStatus.PENDING);

        activeReports.forEach(report -> report.setStatus(ReportStatus.RESOLVED));
        reportRepository.saveAll(activeReports);
    }

    public void fallbackModerateVideo(String videoId, String reason, Throwable throwable){
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "el servicio de video no esta disponible, no se pudo borrar el video");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackModerateUser")
    public void moderateUser(String userId, String reason){
        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminId=principal.userId();

        userClient.banUserInternal(userId);

        AdminEntity action=AdminEntity.builder()
                .adminId(adminId)
                .targetId(userId)
                .reason(reason)
                .actionType("BAN_USER")
                .build();
        adminRepository.save(action);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ADMIN_EXCHANGE, RabbitMQConfig.USER_BAN_RK,
                userId);

        List<ReportEntity> activeReports=reportRepository.findByTargetIdAndTypeAndStatus(userId, ReportType.USER, ReportStatus.PENDING);

        activeReports.forEach(report -> report.setStatus(ReportStatus.RESOLVED));
        reportRepository.saveAll(activeReports);
    }

    public void fallbackModerateUser(String userId, String reason, Throwable throwable){
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "el servicio user no esta disponible, no se pudo banear la cuenta");
    }

    @Override
    public void createReport(ReportType type, String targetId, PredefinedReason reason, String description){
        UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reporterId=principal.userId();

        ReportEntity report=ReportEntity.builder()
                .type(type)
                .targetId(targetId)
                .reporterId(reporterId)
                .reason(reason)
                .reportDescription(description)
                .build();
        reportRepository.save(report);
    }

    @CircuitBreaker(name = "video", fallbackMethod = "fallbackGetReports")
    public List<ReportDetailDto> getPendingReports(){
        //obtener todos los reportes crudos individuales
        List<ReportEntity> rawReports=reportRepository.findByStatus(ReportStatus.PENDING);
        if (rawReports.isEmpty()) return List.of();

        //agrupar los recursos   reportados  con todos sus reportes en un mapa clave valor, siendo el id de el recurso la clave y la lista de todos sus reportes el valor
        Map<String, List<ReportEntity>> groupedByTarget = rawReports.stream()
                .collect(Collectors.groupingBy(r -> r.getTargetId() + "-" + r.getType().name()));

        //limpiar los ID's de recursos reportados repetidos para consultar su informacion en los microservicios
        List<String> videoIds= rawReports.stream()
                .filter(r -> r.getType() == ReportType.VIDEO)
                .map(ReportEntity::getTargetId).distinct().toList();

        List<String> userIds=rawReports.stream()
                .filter(r -> r.getType() == ReportType.USER)
                .map(ReportEntity::getTargetId).distinct().toList();

        //pedir la informacion a los microservicios
        Map<String, VideoMetadataDto> videoMap= Collections.emptyMap();
        if (!videoIds.isEmpty()){
            try {
                videoMap = videoClient.getVideoDetails(videoIds).stream()
                        .collect(Collectors.toMap(VideoMetadataDto::getId, v -> v));
            }catch (Exception e) {
log.error("error al obtener la informacion de los videos: ", e);
            }
        }

        Map<String, UserProfileDto> userMap=Collections.emptyMap();
        if (!userIds.isEmpty()){
            try {
                userMap = userClient.getProfileDetails(userIds).stream()
                        .collect(Collectors.toMap(UserProfileDto::getId, u -> u));
            }catch (Exception e){
log.error("error al obtener la informacion de los usuarios: ", e);
            }
        }

        //declarar como final a los mapas para que java sepa que no se van a modificar
        final Map<String, VideoMetadataDto> finalVideoMap= videoMap;
        final Map<String, UserProfileDto> finalUserMap= userMap;

        //mapear cada objeto a un ReportDetailDto
        return groupedByTarget.entrySet().stream().map(entry -> {
            List<ReportEntity> reports=entry.getValue();
            ReportEntity sample=reports.get(0);
            String targetId= sample.getTargetId();

            //calcular repeticion de razones concretas
            Map<String, Long> reasonCount = reports.stream()
                    .collect(Collectors.groupingBy(r -> r.getReason().name(), Collectors.counting()));

            //obtener todos los comentarios de los reportes
            List<ReportDetailDto.IndividualReportDto> commentsReports=reports.stream()
                    .map(r -> new ReportDetailDto.IndividualReportDto(
                            r.getId(),
                            r.getReason().name(),
                            r.getReportDescription() != null ? r.getReportDescription() : "",
                            r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
                    )).toList();


                    ReportDetailDto dto=ReportDetailDto.builder()
                            .type(sample.getType())
                            .targetId(targetId)
                            .status(sample.getStatus().name())
                            .totalReports(reports.size())
                            .reasonsCount(reasonCount)
                            .reportDescriptions(commentsReports)
                            .build();

if (sample.getType()== ReportType.VIDEO && finalVideoMap.containsKey(targetId)){
VideoMetadataDto v=finalVideoMap.get(targetId);
dto.setVideoUserId(v.getUserId());
dto.setVideoTitle(v.getTitle());
dto.setVideoDescription(v.getDescription());
dto.setVideoDuration(v.getDuration());
dto.setVideoUrl(v.getVideoUrl());
dto.setThumbnailUrl(v.getThumbnailUrl());
dto.setVideoStatus(v.getVideoStatus());
dto.setVideoCreatedAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : null);
}else if (sample.getType()==ReportType.USER && finalUserMap.containsKey(targetId)){
UserProfileDto u=finalUserMap.get(targetId);
dto.setTargetUsername(u.getUsername());
dto.setProfilePictureUrl(u.getProfilePictureUrl());
}

                    return dto;
                } ).toList();
    }

    public List<ReportDetailDto> fallbackGetReports(Throwable throwable){
        return List.of();
    }

    //descartar un reporte si no hay nada malo
    public void dismissReport(String targetId, ReportType type){
        List<ReportEntity> reports=reportRepository.findByTargetIdAndTypeAndStatus(targetId, type, ReportStatus.PENDING);

        if (reports.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no se encontraron reportes pendientes para este recurso.");
        }

//descartar todos los reportes
        reports.forEach(report -> report.setStatus(ReportStatus.DISMISSED));
        reportRepository.saveAll(reports);
    }

}
