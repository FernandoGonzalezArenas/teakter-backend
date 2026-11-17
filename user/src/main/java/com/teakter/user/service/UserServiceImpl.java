package com.teakter.user.service;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserDto;
import com.teakter.user.dto.UserDtoEntrada;
import com.teakter.user.dto.UserPrincipal;
import com.teakter.user.entity.UserEntity;
import com.teakter.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
private final StorageService storageService;

    @Override
public UserDto getProfile(String id){
UserEntity user = repository.findByPublicUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario no encontrado"));
return entityToDto(user);
    }

    @Override
    public UploadUrlResponse getUploadUrl(String filename){
        return storageService.generateUploadUrl(filename);
    }

@Override
public UserDto updateProfile( UserDtoEntrada profileDetails){
    UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String id=principal.userId();

    UserEntity user = repository.findByPublicUserId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario no encontrado"));
    user.setBio(profileDetails.getBio());
    user.setProfilePictureUrl(profileDetails.getProfilePictureUrl());
    user.setPrivacyLikes(profileDetails.getPrivacyLikes());
    user.setPrivacySubs(profileDetails.getPrivacySubs());
     repository.save(user);
     return entityToDto(user);
    }

@Override
public UserDto createInitialProfile(Long id, String publicUserId, String username){
        UserEntity user=new UserEntity();
    user.setId(id);
    user.setPublicUserId(publicUserId);
    user.setUsername(username);
    user.setCreatedAt(Instant.now());
     repository.save(user);
     return entityToDto(user);
}

    @Override
    public List<UserDto> getProfilesBatch(List<String> userIds){
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin=auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "acceso denegado a este recurso, necesitas credenciales de administrador");
        }

        List<UserEntity> users =repository.findAllByPublicUserId(userIds);


        return users.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void banUserInternal(String userId){
        //obtenemos los datos de el usuario que hace la peticion
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        //verificamos si es admin
        boolean isAdmin=auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no tienes permisos de administrador para borrar la cuenta");
        }

UserEntity user=repository.findByPublicUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "el usuario que se quiere bannear no existe"));

        repository.deleteById(user.getId());
    }

    private UserDto entityToDto(UserEntity user){
        String foto=user.getProfilePictureUrl();
        String finalUrl=(foto!=null) ?storageService.getPublicUrl(foto) : null;
        return UserDto.builder()
                .id(user.getPublicUserId())
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePictureUrl(finalUrl)
                .privacyLikes(user.getPrivacyLikes())
                .privacySubs(user.getPrivacySubs())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

}
