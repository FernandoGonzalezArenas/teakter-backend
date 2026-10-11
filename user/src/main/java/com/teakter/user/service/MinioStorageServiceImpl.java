package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements  StorageService{

private final MinioClient minioClient;

@Value("${minio.bucket-users}")
    private String bucketName;

@Value("${minio.url}")
    private String miniourl;

@Override
    public UploadUrlResponse generateUploadUrl(String filename){
    String cleanFilename=sanitizeFilename(filename);
    String finalFilename=UUID.randomUUID()+"-"+cleanFilename;
    String objectName="profiles/"+ finalFilename;
    try {
        String urlFirmada= minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(objectName)
                        .expiry(15, TimeUnit.MINUTES)
                .build());

        String url=urlFirmada.replace("http://minio:9000", "http://localhost:8080/storage");
        return new UploadUrlResponse(url, finalFilename);
    }catch (Exception e){
throw new RuntimeException("error en minio user: " +e.getMessage());
    }
}

@Override
    public String getPublicUrl(String objectName){

    String url= miniourl+"/"+bucketName+"/profiles/"+objectName;
    return url.replace("http://minio:9000", "http://localhost:8080/storage");
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
