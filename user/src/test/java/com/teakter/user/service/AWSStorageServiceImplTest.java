package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserPrincipal;
import com.teakter.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AWSStorageServiceImplTest {

@Mock
    private UserRepository repository;
@Mock
    private S3Presigner s3Presigner;
@Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

@InjectMocks
    private AWSStorageServiceImpl service;
private final Long userId=1L;

@BeforeEach
    void setup(){
    ReflectionTestUtils.setField(service, "bucketName", "bucket-users-test");

    UserPrincipal principal=new UserPrincipal(userId, "userTest");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    @DisplayName("debe generar la URL firmada para la foto de perfil con AWS")
    void shouldGenerateUploadUrlForProfile() throws Exception{
    String filename="profile.jpg";
    String fakeUrl = "https://aws-bucket.s3.amazonaws.com/profiles/uuid-test.mp4?signature=123";

    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
    when(presignedPutObjectRequest.url()).thenReturn(new URL(fakeUrl));

    UploadUrlResponse response=service.generateUploadUrl(filename);

    assertNotNull(response);
    assertEquals(fakeUrl, response.uploadUrl());
    assertTrue(response.uploadUrl().contains("/profiles"));
    assertTrue(response.filename().contains(filename));
}

@Test
    @DisplayName("debe lanzar una excepcion si el servicio faya al generar URL de subida para el perfil")
    void shouldThrowExceptionWhenAWSFails() throws Exception{
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
            .thenThrow(SdkClientException.create("AWS service unavailable"));

    assertThrows(SdkClientException.class, () ->{
        service.generateUploadUrl("error-profile.jpg");
    });

    verify(presignedPutObjectRequest, never()).url();
}

}
