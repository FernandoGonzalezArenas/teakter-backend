package com.teakter.auth.security;

import com.teakter.auth.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

private final String publicUserId;
private final String email;
private final UserEntity userEntity;

public CustomUserDetails(UserEntity userEntity, Collection<? extends GrantedAuthority> authorities){
    super(userEntity.getUsername(), userEntity.getPassword(), authorities);
    this.publicUserId= userEntity.getPublicUserId();
    this.email=userEntity.getEmail();
    this.userEntity=userEntity;
}

}
