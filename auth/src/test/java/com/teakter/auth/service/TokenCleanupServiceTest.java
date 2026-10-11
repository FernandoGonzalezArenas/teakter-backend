package com.teakter.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;

import com.teakter.auth.repository.UserRepository;
import com.teakter.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.teakter.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @InjectMocks
    private TokenCleanupService cleanupService;

@Test
void  deleteExpiredTokens_invocaRepoConAhora(){
    cleanupService.deleteExpiredTokens();
    verify(refreshTokenRepository, times(1)).deleteAllByExpiryDateBefore(any(Instant.class));
    verify(verificationTokenRepository, times(1)).deleteAllByExpiryDateBefore(any(LocalDateTime.class));
}

}
