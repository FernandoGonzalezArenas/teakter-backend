package com.teakter.video.service;

import com.teakter.video.dto.UploadUrlResponse;
import com.teakter.video.repository.VideoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "aws")
public class AWSVideoServiceImpl extends AbstractVideoService {

    private final S3Presigner s3Presigner;

@Value("${aws.s3.bucket-videos}")
    private String bucketName;

@Value("${aws.cloudfront.domain.videos}")
private String cloudfrontDomainVideos;

    public AWSVideoServiceImpl(VideoRepository videoRepository, InteractionsService interactionsService, LikeService likeService, SubscriptionService subscriptionService, UserService userService, RabbitTemplate rabbitTemplate, S3Presigner s3Presigner){
    super(videoRepository, interactionsService, likeService, subscriptionService, userService, rabbitTemplate);
    this.s3Presigner=s3Presigner;
}

//metodo para obtener la URL prefirmada de AWS S3
@Override
    public UploadUrlResponse generateUploadUrl(String filename){
        String cleanFilename=sanitizeFilename(filename);
String finalFileName=UUID.randomUUID().toString() + "-" + cleanFilename;
String objectName="videos/" + finalFileName;
        PutObjectPresignRequest presignRequest=PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(req -> req.bucket(bucketName).key(objectName))
                .build();

        PresignedPutObjectRequest presignedRequest= s3Presigner.presignPutObject(presignRequest);
        return new UploadUrlResponse(presignedRequest.url().toString(), finalFileName);
    }

    @Override
    protected String buildFullVideoUrl(String filename){
    return "https://" + cloudfrontDomainVideos + "/videos/" + filename;
    }

    //metodo para obtener la URL prefirmada de AWS S3 para miniaturas
    @Override
    public UploadUrlResponse generateUploadUrlThumb(String filename){
        String cleanFilename=sanitizeFilename(filename);
        String finalFileName=UUID.randomUUID().toString() + "-" + cleanFilename;
        String objectName="thumbnails/" + finalFileName;
        PutObjectPresignRequest presignRequest=PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(req -> req.bucket(bucketName).key(objectName))
                .build();

        PresignedPutObjectRequest presignedRequest= s3Presigner.presignPutObject(presignRequest);
        return new UploadUrlResponse(presignedRequest.url().toString(), finalFileName);
    }

    @Override
    protected String buildFullThumbnailUrl(String filename){
        return "https://" + cloudfrontDomainVideos + "/thumbnails/" + filename;
    }

    @Override
protected String getPlaybackUrl(String filename){
        String baseName=filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
        return "https://" + cloudfrontDomainVideos + "/videos/hls/" + baseName + "/master.m3u8";
}

}
