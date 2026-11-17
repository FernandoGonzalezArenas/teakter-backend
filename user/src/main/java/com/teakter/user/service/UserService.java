package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserDto;
import com.teakter.user.dto.UserDtoEntrada;
import com.teakter.user.entity.UserEntity;

import java.util.List;

public interface UserService {

UserDto getProfile(String id);

UploadUrlResponse getUploadUrl(String filename);

UserDto updateProfile(UserDtoEntrada profileDetails);

UserDto createInitialProfile(Long id, String publicUserId, String username);

List<UserDto> getProfilesBatch(List<String> userIds);

void banUserInternal(String userId);

}
