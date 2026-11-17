package com.teakter.video.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.Arrays;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "aws")
public class AWSConfig {

    @Autowired
    private Environment env;

@Value("${aws.s3.region}")
    private String region;

@Value("${aws.access-key}")
    private String accessKey;

@Value("${aws.secret-key}")
    private String secretKey;

@Value("${aws.s3.bucket-videos}")
    private String bucketName;

@Bean
    public S3Client s3Client() throws Exception{
    S3Client s3=S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)))
            .httpClientBuilder(ApacheHttpClient.builder()
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(10)) //tiempo maximo para conectar
                    .socketTimeout(Duration.ofSeconds(30)) //tiempo maximo de inactividad
                    .connectionMaxIdleTime(Duration.ofSeconds(60))) //evita que las conecciones se queden colgadas o con DNS viejo expirado
            .build();

    boolean isTest= Arrays.asList(env.getActiveProfiles()).contains("test");
    if (!isTest) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }
    return s3;
}

    @Bean
    public S3Presigner s3Presigner(){
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                )).build();
    }

}
