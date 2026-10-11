package com.teakter.video.listener;

import com.teakter.video.config.RabbitMQConfig;
import com.teakter.video.dto.VideoProcessEvent;
import com.teakter.video.service.HlsTranscoderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoTranscoderListener {

private final HlsTranscoderService hlsTranscoderService;

@RabbitListener(queues = RabbitMQConfig.VIDEO_PROCESS_HLS_QUEUE)
    public void handleVideoProcess(VideoProcessEvent event){
    log.info("iniciando procesamiento HLS para el video con ID: {}", event.videoId());
        hlsTranscoderService.processVideoToHls(event.rawFilename(), event.videoId());
        log.info("video con el ID: {} procesado exitosamente a HLS", event.videoId());
}

}
