package com.teakter.video.service;

import com.teakter.video.dto.UploadUrlResponse;
import com.teakter.video.repository.VideoRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioVideoServiceImpl extends AbstractVideoService{

private final MinioClient minioClient;

@Value("${minio.bucket-videos}")
    private String bucketName;

@Value("${minio.url}")
private String minioUrl;

    public MinioVideoServiceImpl(VideoRepository videoRepository, InteractionsService interactionsService, LikeService likeService, SubscriptionService subscriptionService, UserService userService, RabbitTemplate rabbitTemplate, MinioClient minioClient){
    super(videoRepository, interactionsService, likeService, subscriptionService, userService, rabbitTemplate);
    this.minioClient=minioClient;
}

    //metodo para generar una URL firmada para subir videos a MinIO
    @Override
    public UploadUrlResponse generateUploadUrl(String filename){
        String cleanFilename=sanitizeFilename(filename);
        String finalFileName=UUID.randomUUID().toString()+"-"+cleanFilename;
        String objectName="videos/"+finalFileName;
        try {
            //configurar la solicitud de URL firmada
            String presignedUrl =minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.PUT)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            String url=presignedUrl.replace("http://minio:9000", "http://localhost:8080/storage");
            return new UploadUrlResponse(url, finalFileName);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error al generar URL firmada"+e.getMessage());
        }
    }

    //metodo para generar una URL firmada para subir miniaturas a MinIO
    @Override
    public UploadUrlResponse generateUploadUrlThumb(String filename){
        String cleanFilename=sanitizeFilename(filename);
        String finalFileName=UUID.randomUUID().toString()+"-"+cleanFilename;
        String objectName="thumbnails/"+finalFileName;
        try {
            //configurar la solicitud de URL firmada
            String presignedUrl =minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.PUT)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            String url=presignedUrl.replace("http://minio:9000", "http://localhost:8080/storage");
            return new UploadUrlResponse(url, finalFileName);
        }catch (Exception e){
            throw new RuntimeException("error al generar URL firmada"+e.getMessage());
        }
    }

    @Override
    protected String buildFullVideoUrl(String filename){
return minioUrl + "/" + bucketName + "/videos/" + filename;
    }

    @Override
    protected String buildFullThumbnailUrl(String filename){
        String url = minioUrl + "/" + bucketName + "/thumbnails/" + filename;
        return url.replace("http://minio:9000", "http://localhost:8080/storage");
    }

    @Override
    protected String getPlaybackUrl(String filename){
        //extraemos el nombre de el objeto
    String objectName="videos/"+filename;
    try {
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                .method(Method.GET) //metodo GET para lectura
                .bucket(bucketName)
                .object(objectName)
                .expiry(2, TimeUnit.HOURS)
                .build());

        return url.replace("http://minio:9000", "http://localhost:8080/storage");
    }catch (Exception e){
        log.error("error generando URL de reproduccion minio: {}", e);
        return filename;
    }
}

}
