package com.teakter.auth.service;

import com.teakter.auth.dto.AuthResponse;
import com.teakter.auth.dto.DataJwtUser;
import com.teakter.auth.dto.LoginRequest;
import com.teakter.auth.dto.RegisterRequest;

public interface AuthService {

    public void register(RegisterRequest request);

    public AuthResponse login(LoginRequest request);

    void banUser(Long userId);

    void verifyAccount(String token);

    DataJwtUser getUserById(Long userId);

}
