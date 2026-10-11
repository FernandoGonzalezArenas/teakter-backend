package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "aws")
public class AWSStorageServiceImpl implements StorageService{

private final S3Presigner s3Presigner;

@Value("${aws.s3.bucket-users}")
    private String bucketName;

@Value("${aws.cloudfront.domain.users}")
private String cloudfrontDomainUsers;

@Override
    public UploadUrlResponse generateUploadUrl(String filename){
    String cleanFilename=sanitizeFilename(filename);
    String finalFilename= UUID.randomUUID().toString()+"-"+cleanFilename;
    String objectName="profiles/"+finalFilename;
    System.out.println("obteniendo la URL firmada para subir la foto de perfil a AWS");
    PutObjectPresignRequest presignRequest=PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(req -> req.bucket(bucketName).key(objectName))
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    System.out.println("retornando la URL firmada de subida y el nombre unico de la foto de perfil almacenada en AWS");
    return new UploadUrlResponse(presignedRequest.url().toString(), finalFilename);
}

@Override
    public String getPublicUrl(String filename){
    System.out.println("retornando la URL de la foto de perfil almacenada en AWS");
    return "https://"+cloudfrontDomainUsers+"/profiles/"+filename;
}

    protected String sanitizeFilename(String filename){
        if (filename == null) return null;

        //remuebe espacios multiples, tabulaciones y saltos de linea remplazandolos por "_" ademas de remover caracteres especiales
        int lastDot=filename.lastIndexOf('.');
        String name=(lastDot != -1) ? filename.substring(0, lastDot) : filename;
        String extencion=(lastDot != -1) ? filename.substring(lastDot) : "";

        String cleanName= name.replaceAll("\\s+", "_");
        cleanName=cleanName.replaceAll("[^a-zA-Z0-9_-]", "");
        String cleanExtencion=extencion.replaceAll("[^a-zA-Z0-9.]", "").toLowerCase();
        System.out.println("el nombre de el archivo quedo como: "+ (cleanName + cleanExtencion));

        return cleanName + cleanExtencion;
    }

}
