package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserPrincipal;
import com.teakter.user.repository.UserRepository;
import io.minio.MinioClient;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MinioStorageServiceImplTest {

@Mock
    private UserRepository repository;
@Mock
    private MinioClient minioClient;

@InjectMocks
private MinioStorageServiceImpl service;

private final Long userId=55L;

@BeforeEach
    void setup(){
    ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(service, "miniourl", "http://localhost:9000");

    UserPrincipal principal=new UserPrincipal(userId, "userTest");
    var auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
}

@Test
    @DisplayName("debe generar una URL firmada para la ssubida de la foto de perfil")
    void shouldGenerateUploadUrlForProfile() throws Exception{
    String filename="perfil.jpg";

    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://signed-url.com?signature=xyz");

    UploadUrlResponse response=service.generateUploadUrl(filename);

    assertNotNull(response);
    assertEquals("http://signed-url.com?signature=xyz", response.uploadUrl());
    assertTrue(response.filename().contains(filename));
}

@Test
    @DisplayName("debe verificar la excepcion lanzada si el servicio minio faya")
    void shouldThrowExceptionWhenMinioFails() throws Exception{
    when(minioClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("minio down"));

    assertThrows(RuntimeException.class, () -> service.generateUploadUrl("profile.jpg"));
}

}
